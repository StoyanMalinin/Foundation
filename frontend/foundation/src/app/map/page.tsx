"use client";

import dynamic from 'next/dynamic'
import { useRef, useState, useEffect } from "react"
import Map from "./map"
import { eventNames } from "process";
import { deleteAppClientCache } from "next/dist/server/lib/render-server";
import internal from 'stream';

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

    const [latLon, setLatLon] = useState<LatLon>({lat: [-80, +80], lon: [-170, +170]});
    const [isMouseUp, setMouseUp] = useState<boolean>(true);
    const [size, setSize] = useState<[number, number]>([0, 0]); // [width, height]

    useEffect(() => {
      if (divRef?.current) {
        console.log("set size");
        const bounding = divRef?.current?.getBoundingClientRect();
        setSize([bounding.width, bounding.height]);
      }
    }, []);

    console.log("size", size[0], size[1]);
    const handleMouseMove = (event: MouseEvent) => {      
        if (!divRef?.current) {
          return;
        }

        if (isMouseUp) {
          if (mousePosition.x != null) {
            setMousePosition({x: null, y: null});
          }

          return;
        }

        setMousePosition({x: event.clientX, y: event.clientY});

        if (mousePosition.x != null && mousePosition.y != null) {
            const [width, height] = size;
            const offsetX = (event.clientX - mousePosition.x);
            const offsetY = (event.clientY - mousePosition.y);

            const newLat: [number, number] = [0, 0];
            newLat[0] = latLon.lat[0] + (offsetY / height) * (latLon.lat[1] - latLon.lat[0]);
            newLat[1] = latLon.lat[1] + (offsetY / height) * (latLon.lat[1] - latLon.lat[0]);
            
            const newLon: [number, number] = [0, 0];
            newLon[0] = latLon.lon[0] - (offsetX / width) * (latLon.lon[1] - latLon.lon[0]);
            newLon[1] = latLon.lon[1] - (offsetX / width) * (latLon.lon[1] - latLon.lon[0]);
            
            setLatLon({
              lat: newLat,
              lon: newLon
            });
        }        
      };

      const handleMouseWheel = (event: WheelEvent) => {
        if (!divRef?.current) {
          return;
        }

        if (Math.abs(event.deltaY) > 1) {
          const latDelta = latLon.lat[1] - latLon.lat[0];
          const lonDelta = latLon.lon[1] - latLon.lon[0];

          const scale = Math.exp(event.deltaY / 5000.0);
          
          const newLatDelta = clipBetween(latDelta * scale, 0.5, 180);
          const newLonDelta = clipBetween(lonDelta * scale, 0.5, 360);

          setLatLon({
            lat: [latLon.lat[0] + (latDelta - newLatDelta) / 2, latLon.lat[1] - (latDelta - newLatDelta) / 2],
            lon: [latLon.lon[0] + (lonDelta - newLonDelta) / 2, latLon.lon[1] - (lonDelta - newLonDelta) / 2]
          });
        }
      };

    return <div ref={divRef} key={"map-wrapper-key"} onMouseMove={handleMouseMove} onMouseDown={() => setMouseUp(false)} onMouseUp={() => setMouseUp(true)} onWheel={handleMouseWheel} className="map-wrapper"><Map lat={latLon.lat} lon={latLon.lon} drawWidth={size[0]} drawHeight={size[1]} /></div>
}