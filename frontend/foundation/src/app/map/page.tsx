"use client";

import { useRef, useState } from "react"
import Map from "./map"
import { eventNames } from "process";
import { deleteAppClientCache } from "next/dist/server/lib/render-server";

function clipBetween(x: number, l: number, r: number): number {
  return Math.min(Math.max(x, l), r);
}

export default function MapWrapper() {
    const [mousePosition, setMousePosition] = useState<{x : number | null, y : number | null}>({ x: null, y: null });
    const divRef = useRef(null);

    const [lat, setLat] = useState<[number, number]>([-90, +90]);
    const [lon, setLon] = useState<[number, number]>([-90, +90]);

    const [isMouseUp, setMouseUp] = useState<boolean>(true);

    const handleMouseMove = (event: MouseEvent) => {
        const {width, height} = divRef?.current?.getBoundingClientRect();
        
        if (mousePosition.x != null && mousePosition.y != null && !isMouseUp) {
            const offsetX = (event.clientX - mousePosition.x);
            const offsetY = (event.clientY - mousePosition.y);

            const newLat: [number, number] = [0, 0];
            newLat[0] = lat[0] + (offsetX / width) * (lat[1] - lat[0]);
            newLat[1] = lat[1] + (offsetX / width) * (lat[1] - lat[0]);
            
            const newLon: [number, number] = [0, 0];
            newLon[0] = lon[0] + (offsetY / height) * (lon[1] - lon[0]);
            newLon[1] = lon[1] + (offsetY / height) * (lon[1] - lon[0]);
            
            setLon(newLon);
            setLat(newLat);
        }
        
        setMousePosition({x: event.clientX, y: event.clientY});
      };

      const handleMouseWheel = (event: WheelEvent) => {
        if (Math.abs(event.deltaY) > 1) {
          const latDelta = lat[1] - lat[0];
          const lonDelta = lon[1] - lon[0];

          const scale = Math.exp(event.deltaY / 5000.0);
          
          const newLatDelta = clipBetween(latDelta * scale, 0.5, 180);
          const newLonDelta = clipBetween(lonDelta * scale, 0.5, 180);

          setLat([lat[0] + (latDelta - newLatDelta) / 2, lat[1] - (latDelta - newLatDelta) / 2]);
          setLon([lon[0] + (lonDelta - newLonDelta) / 2, lon[1] - (lonDelta - newLonDelta) / 2]);
        }
      };

    return <div ref={divRef} onMouseMove={handleMouseMove} onMouseDown={() => setMouseUp(false)} onMouseUp={() => setMouseUp(true)} onWheel={handleMouseWheel} className="map-wrapper"><Map lat={lat} lon={lon} ></Map></div>
}