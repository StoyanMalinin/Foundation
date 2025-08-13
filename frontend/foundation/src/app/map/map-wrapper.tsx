"use client";

import { useState, useEffect } from "react"
import Map from "./map"
import { LatLon, recalculateAndNormalizeBoundingBox, normalizeBoundingBox } from "./map-utils"
import Button from '@mui/material/Button';

type MapWrapperProps = {
  divRef: React.RefObject<HTMLDivElement>,
  canvasRef: React.RefObject<HTMLCanvasElement>,
}

export default function MapWrapper(props: MapWrapperProps) {
    // This is only tracked if the mouse is down - saves us from unnecessary re-renders  
    const [mousePosition, setMousePosition] = useState<{x : number | null, y : number | null}>({ x: null, y: null });

    const [latLon, setLatLon] = useState<LatLon>({lat: [-60, +60], lon: [-140, +140]});
    const [isMouseUp, setMouseUp] = useState<boolean>(true);
    const [size, setSize] = useState<[number, number]>([0, 0]); // [width, height]
    const [bounding, setBounding] = useState<[number, number, number, number]>([0, 0, 0, 0]); // [l, r, d, u]

    const handleMouseMove = (event: MouseEvent) => {   
        if (isMouseUp) {
            if (mousePosition.x != null) {
                setMousePosition({x: null, y: null});
            }

            return;
        }

        // Mouse is down

        if (mousePosition.x != null && mousePosition.y != null) { 
            const [width, height] = size;
            const offsetX = (event.clientX - mousePosition.x);
            const offsetY = (event.clientY - mousePosition.y);

            // TODO: make this atomic
            setMousePosition({x: event.clientX, y: event.clientY});
            setLatLon(latLon => {
                const newLat: [number, number] = [0, 0];
                newLat[0] = latLon.lat[0] + (offsetY / height) * (latLon.lat[1] - latLon.lat[0]);
                newLat[1] = latLon.lat[1] + (offsetY / height) * (latLon.lat[1] - latLon.lat[0]);
                
                const newLon: [number, number] = [0, 0];
                newLon[0] = latLon.lon[0] - (offsetX / width) * (latLon.lon[1] - latLon.lon[0]);
                newLon[1] = latLon.lon[1] - (offsetX / width) * (latLon.lon[1] - latLon.lon[0]);
                
                return normalizeBoundingBox({
                    lat: newLat,
                    lon: newLon
                })
            });
        } else { // just set the initial mouse position
            setMousePosition({x: event.clientX, y: event.clientY});
        }        
    };

    const handleMouseWheel = (event: WheelEvent) => {
        if (Math.abs(event.deltaY) > 1) {
            const scale = Math.exp(event.deltaY / 1000.0);

            const focusX = (event.clientX - bounding[0]) / size[0];
            const focusY = (event.clientY - bounding[3]) / size[1];
            const focus: [number, number] = [focusX, focusY];

            setLatLon(latLon => recalculateAndNormalizeBoundingBox(scale, latLon.lat, latLon.lon, focus));
        }
    };

    // TODO: figure out why this is needed
    if (props.divRef.current) {
        props.divRef.current.onmousemove = handleMouseMove;
        props.divRef.current.onmouseup = () => setMouseUp(true);
        props.divRef.current.onmousedown = () => setMouseUp(false);
        props.divRef.current.onwheel = handleMouseWheel;
    }

    useEffect(() => {
        if (props.divRef.current) {
            const bounding = props.divRef.current.getBoundingClientRect();

            // TODO: make this atomic
            setBounding([bounding.left, bounding.right, bounding.bottom, bounding.top]);
            setSize([bounding.width, bounding.height]);
        }
    }, [props.divRef]);

    // If the size is 0 - the div is not rendered yet
    if (size[0] === 0) {
        return <></>;
    }

    return <>
        <Map lat={latLon.lat} lon={latLon.lon} drawWidth={size[0]} drawHeight={size[1]} canvasRef={props.canvasRef} />
        <Button onClick={() => setLatLon({lat: [-80, +80], lon: [-170, +170]})}>Reset</Button>
        <Button onClick={() => setLatLon(recalculateAndNormalizeBoundingBox(2, latLon.lat, latLon.lon, [0.5, 0.5]))}>-</Button>
        <Button onClick={() => setLatLon(recalculateAndNormalizeBoundingBox(0.5, latLon.lat, latLon.lon, [0.5, 0.5]))}>+</Button>
    </>;
}