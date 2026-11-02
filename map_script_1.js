(function(){
 const status=document.getElementById('mapstatus');
 if(typeof L==='undefined'){status.textContent='MAP ENGINE UNAVAILABLE — CHECK INTERNET';return;}
 const map=L.map('map',{zoomControl:false,attributionControl:true,preferCanvas:true,fadeAnimation:false,zoomAnimation:false}).setView([53.4808,-2.2426],11);
 let activeProvider=0, providerTimer=null, tileErrors=0, tilesLoaded=0, baseLayer=null, roadsLayer=null;
 const providers=[
  {name:'Carto Voyager road map',base:'https://{s}.basemaps.cartocdn.com/rastertiles/voyager/{z}/{x}/{y}{r}.png',subdomains:'abcd',attribution:'© OpenStreetMap contributors © CARTO'},
  {name:'Satellite + road labels',base:'https://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/{z}/{y}/{x}',roads:'https://server.arcgisonline.com/ArcGIS/rest/services/Reference/World_Transportation/MapServer/tile/{z}/{y}/{x}',attribution:'Tiles © Esri — Sources: Esri, Maxar, Earthstar Geographics'},
  {name:'OpenStreetMap',base:'https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png',subdomains:'abc',attribution:'© OpenStreetMap contributors'},
  {name:'OSM France',base:'https://{s}.tile.openstreetmap.fr/osmfr/{z}/{x}/{y}.png',subdomains:'abc',attribution:'© OpenStreetMap contributors · OSM France'}
 ];
 function clearLayers(){if(baseLayer)map.removeLayer(baseLayer);if(roadsLayer){map.removeLayer(roadsLayer);roadsLayer=null;}}
 function useProvider(index){
  if(index>=providers.length){status.style.display='block';status.textContent='MAP TILES UNAVAILABLE — CHECK INTERNET';return;}
  activeProvider=index;tilesLoaded=0;tileErrors=0;clearLayers();
  const p=providers[index];status.style.display='block';status.textContent='LOADING '+p.name.toUpperCase()+'…';
  const opts={maxZoom:19,keepBuffer:3,updateWhenIdle:true,crossOrigin:true,attribution:p.attribution};if(p.subdomains)opts.subdomains=p.subdomains;
  baseLayer=L.tileLayer(p.base,opts);
  let overlayErrors=0,overlayLoaded=0;
  function loaded(){if(tilesLoaded>=3 && (!p.roads || overlayLoaded>=2)){status.style.display='none';if(providerTimer){clearTimeout(providerTimer);providerTimer=null;}}}
  baseLayer.on('tileload',function(){tilesLoaded++;loaded();});
  baseLayer.on('tileerror',function(){tileErrors++;maybeFallback();});
  baseLayer.addTo(map);
  if(p.roads){
   roadsLayer=L.tileLayer(p.roads,{maxZoom:19,keepBuffer:3,updateWhenIdle:true,crossOrigin:true,opacity:.88,attribution:'Roads & labels © Esri'});
   roadsLayer.on('tileload',function(){overlayLoaded++;loaded();});
   roadsLayer.on('tileerror',function(){overlayErrors++;maybeFallback();});
   roadsLayer.addTo(map);
  }
  function maybeFallback(){if((tilesLoaded<3 && tileErrors>=3)||(p.roads && overlayErrors>=3 && overlayLoaded<2)){if(!providerTimer){providerTimer=setTimeout(()=>{providerTimer=null;if(activeProvider===index&&((tilesLoaded<3)||(p.roads&&overlayLoaded<2)))useProvider(index+1);},700);}}}
  if(providerTimer)clearTimeout(providerTimer);
  providerTimer=setTimeout(()=>{providerTimer=null;if(activeProvider===index&&((tilesLoaded<3)||(p.roads&&overlayLoaded<2)))useProvider(index+1);},8000);
 }
 useProvider(0);
 let routeLayer=L.polyline([],{color:'#168fe8',weight:6,opacity:1,lineCap:'round',lineJoin:'round'}).addTo(map);
 let routeCasing=L.polyline([],{color:'#062d4e',weight:11,opacity:.96,lineCap:'round',lineJoin:'round'}).addTo(map);
 let travelledLayer=L.polyline([],{color:'#91a4b2',weight:5,opacity:.9,lineCap:'round',lineJoin:'round'}).addTo(map);
 let routeCoords=[],plannedRouteLoaded=false,autoFollow=true,lastVehicle=null,lastFollowRequest=0,lastStyleRequest=0,lastRouteMatchIndex=0;
 window.haulwaySetRoute=function(jsonText){try{const raw=JSON.parse(jsonText);const coords=raw.map(c=>[Number(c[1]),Number(c[0])]).filter(c=>Number.isFinite(c[0])&&Number.isFinite(c[1]));if(coords.length<2)throw new Error('Route geometry is empty');routeCoords=coords;lastRouteMatchIndex=0;routeCasing.setLatLngs(coords);routeLayer.setLatLngs(coords);travelledLayer.setLatLngs([]);plannedRouteLoaded=true;map.fitBounds(routeLayer.getBounds(),{padding:[45,45]});}catch(e){status.style.display='block';status.textContent='ROUTE DISPLAY ERROR';console.log(e);}};
 let userMarker=null,userCircle=null,firstGpsFix=true;
 const vehicleIcon=L.divIcon({className:'',html:'<div class="haulway-position"></div>',iconSize:[30,38],iconAnchor:[15,19]});
 window.haulwaySetZoom=function(offset){const target=Math.max(5,Math.min(18,12+Number(offset||0)));map.setZoom(target);};
 window.haulwayRecenter=function(){autoFollow=true;if(userMarker)map.setView(userMarker.getLatLng(),Math.max(map.getZoom(),15),{animate:true});};
 map.on('dragstart',function(){autoFollow=false;});
 function closestPointOnSegment(point,a,b){
  const cos=Math.cos(point[0]*Math.PI/180);
  const ax=a[1]*cos, ay=a[0], bx=b[1]*cos, by=b[0], px=point[1]*cos, py=point[0];
  const dx=bx-ax, dy=by-ay, denom=dx*dx+dy*dy;
  const t=denom===0?0:Math.max(0,Math.min(1,((px-ax)*dx+(py-ay)*dy)/denom));
  const snapped=[ay+t*dy,(ax+t*dx)/Math.max(cos,0.000001)];
  return {point:snapped,distance:map.distance(point,snapped),t:t};
 }
 function routeMatch(point){
  if(routeCoords.length<2)return null;
  let best={distance:Infinity,index:0,point:routeCoords[0]};
  // Search a small window behind and a generous window ahead of the last match.
  // This prevents GPS jitter at junctions from making the travelled route jump backwards.
  const first=Math.max(0,lastRouteMatchIndex-2);
  const last=Math.min(routeCoords.length-2,lastRouteMatchIndex+180);
  for(let i=first;i<=last;i++){
   const candidate=closestPointOnSegment(point,routeCoords[i],routeCoords[i+1]);
   if(candidate.distance<best.distance)best={distance:candidate.distance,index:i,point:candidate.point};
  }
  if(best.distance<120)lastRouteMatchIndex=Math.max(lastRouteMatchIndex,best.index);
  return best;
 }
 function updateVehicle(lat,lon,accuracy,bearing){
  const ll=[lat,lon];lastVehicle=ll;
  const match=routeMatch(ll);
  const offRoute=!!(match&&match.distance>90);
  const ringColor=offRoute?'#ff9b38':'#168fe8';
  if(!userMarker){userMarker=L.marker(ll,{icon:vehicleIcon,zIndexOffset:1000}).addTo(map);userCircle=L.circle(ll,{radius:Math.max(accuracy||10,10),color:ringColor,weight:1,fillColor:ringColor,fillOpacity:.10}).addTo(map);}
  else{userMarker.setLatLng(ll);userCircle.setLatLng(ll).setRadius(Math.max(accuracy||10,10)).setStyle({color:ringColor,fillColor:ringColor});}
  const el=userMarker.getElement();
  if(el&&Number.isFinite(bearing)){const inner=el.querySelector('.haulway-position');if(inner)inner.style.transform='rotate('+bearing+'deg)';}
  if(match && match.distance<120){travelledLayer.setLatLngs(routeCoords.slice(0,match.index+1).concat([match.point]));}
  if(autoFollow&&(firstGpsFix||!plannedRouteLoaded)){map.setView(ll,Math.max(map.getZoom(),15),{animate:!firstGpsFix});}
  else if(autoFollow){map.panTo(ll,{animate:true,duration:.35});}
  firstGpsFix=false;
 }
 window.haulwaySetVehicleLocation=function(lat,lon,bearing,accuracy){if(Number.isFinite(lat)&&Number.isFinite(lon))updateVehicle(lat,lon,Number(accuracy)||18,bearing);};
 window.haulwayFollowRequest=function(requestId){if(requestId!==lastFollowRequest){lastFollowRequest=requestId;autoFollow=true;if(userMarker)map.setView(userMarker.getLatLng(),Math.max(map.getZoom(),14),{animate:true});}};
 window.haulwaySetMapStyleRequest=function(requestId){if(requestId!==lastStyleRequest){lastStyleRequest=requestId;useProvider(activeProvider===1?0:1);}};
 // GPS updates arrive from Android LocationManager; no duplicate WebView watcher.
 setTimeout(()=>map.invalidateSize(true),250);setTimeout(()=>map.invalidateSize(true),900);window.addEventListener('resize',()=>map.invalidateSize(true));
})();
