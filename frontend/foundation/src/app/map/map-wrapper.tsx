"use client";

import { useState, useEffect } from "react"
import Map from "./map"

function clipBetween(x: number, l: number, r: number): number {
  return Math.min(Math.max(x, l), r);
}

type LatLon = {
  lat: [number, number],
  lon: [number, number],
}

type MapWrapperProps = {
  divRef: React.RefObject<HTMLDivElement>,
  canvasRef: React.RefObject<HTMLCanvasElement>,
}

export default function MapWrapper(props: MapWrapperProps) {
    console.log("Render MapWrapper");
  
    const [mousePosition, setMousePosition] = useState<{x : number | null, y : number | null}>({ x: null, y: null });

    const [latLon, setLatLon] = useState<LatLon>({lat: [-80, +80], lon: [-170, +170]});
    const [isMouseUp, setMouseUp] = useState<boolean>(true);
    const [size, setSize] = useState<[number, number]>([0, 0]); // [width, height]

    useEffect(() => {
      if (props.divRef.current) {
        const bounding = props.divRef.current?.getBoundingClientRect();
        setSize([bounding.width, bounding.height]);
      }
    }, [props.divRef]);

    const handleMouseMove = (event: MouseEvent) => {      
        if (!props.divRef.current) {
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
        if (!props.divRef.current) {
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

      if (props.divRef.current) {
        props.divRef.current.onmousemove = handleMouseMove;  
        props.divRef.current.onmousedown = () => setMouseUp(false);
        props.divRef.current.onmouseup = () => setMouseUp(true);
        props.divRef.current.onwheel = handleMouseWheel;
      }

    return <Map lat={latLon.lat} lon={latLon.lon} drawWidth={size[0]} drawHeight={size[1]} canvasRef={props.canvasRef} />;
}