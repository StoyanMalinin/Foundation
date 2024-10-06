"use client";

import dynamic from 'next/dynamic'
import { useRef, useState } from "react"
import Map from "./map"
import { eventNames } from "process";
import { deleteAppClientCache } from "next/dist/server/lib/render-server";

function clipBetween(x: number, l: number, r: number): number {
  return Math.min(Math.max(x, l), r);
}

type LatLon = {
  lat: [number, number],
  lon: [number, number],
}

export default function MapWrapper() {
    const [mousePosition, setMousePosition] = useState<{x : number | null, y : number | null}>({ x: null, y: null });
    const divRef = useRef(null);

    const [latLon, setLatLon] = useState<LatLon>({lat: [-90, +90], lon: [-90, +90]});
    const [isMouseUp, setMouseUp] = useState<boolean>(true);

    const handleMouseMove = (event: MouseEvent) => {
        if (isMouseUp) {
          if (mousePosition.x != null) {
            setMousePosition({x: null, y: null});
          }

          return;
        }

        const {width, height} = divRef?.current?.getBoundingClientRect();
        setMousePosition({x: event.clientX, y: event.clientY});

        if (mousePosition.x != null && mousePosition.y != null) {
            const offsetX = (event.clientX - mousePosition.x);
            const offsetY = (event.clientY - mousePosition.y);

            const newLat: [number, number] = [0, 0];
            newLat[0] = latLon.lat[0] + (offsetY / width) * (latLon.lat[1] - latLon.lat[0]);
            newLat[1] = latLon.lat[1] + (offsetY / width) * (latLon.lat[1] - latLon.lat[0]);
            
            const newLon: [number, number] = [0, 0];
            newLon[0] = latLon.lon[0] - (offsetX / height) * (latLon.lon[1] - latLon.lon[0]);
            newLon[1] = latLon.lon[1] - (offsetX / height) * (latLon.lon[1] - latLon.lon[0]);
            
            setLatLon({
              lat: newLat,
              lon: newLon
            });
        }        
      };

      const handleMouseWheel = (event: WheelEvent) => {
        if (Math.abs(event.deltaY) > 1) {
          const latDelta = latLon.lat[1] - latLon.lat[0];
          const lonDelta = latLon.lon[1] - latLon.lon[0];

          const scale = Math.exp(event.deltaY / 5000.0);
          
          const newLatDelta = clipBetween(latDelta * scale, 0.5, 180);
          const newLonDelta = clipBetween(lonDelta * scale, 0.5, 180);

          setLatLon({
            lat: [latLon.lat[0] + (latDelta - newLatDelta) / 2, latLon.lat[1] - (latDelta - newLatDelta) / 2],
            lon: [latLon.lon[0] + (lonDelta - newLonDelta) / 2, latLon.lon[1] - (lonDelta - newLonDelta) / 2]
          });
        }
      };

    return <div ref={divRef} onMouseMove={handleMouseMove} onMouseDown={() => setMouseUp(false)} onMouseUp={() => setMouseUp(true)} onWheel={handleMouseWheel} className="map-wrapper"><Map lat={latLon.lat} lon={latLon.lon} ></Map></div>
}