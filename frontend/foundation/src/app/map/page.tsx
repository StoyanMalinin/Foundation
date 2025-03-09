"use client";

import { useRef } from "react";
import MapWrapper from "./map-wrapper";

export default function MapPage() {
    console.log("Render MapPage");

    const divRef = useRef<HTMLDivElement>(null);
    const canvasRef = useRef<HTMLCanvasElement>(null);

    return (
        <div ref={divRef} key={"map-wrapper-key"} className="map-wrapper">
            <canvas id="map-canvas" ref={canvasRef} style={{width: "100%", height: "100%", objectFit: "contain"}} />
            <MapWrapper divRef={divRef} canvasRef={canvasRef} />        
        </div>
    );
}