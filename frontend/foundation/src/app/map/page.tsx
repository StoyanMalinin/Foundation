"use client";

import { useRef } from "react";
import MapWrapper from "./map-wrapper";
import { useSearchParams } from "next/navigation";
import { NavigationBar } from "../navigation/navigation-bar";

export default function MapPage() {
    const searchParams = useSearchParams();
    const divRef = useRef<HTMLDivElement>(null);
    const canvasRef = useRef<HTMLCanvasElement>(null);

    const id = searchParams.get("id");
    if (id == null) return <p>You should specify search ID</p>;

    return (
        <>
            <NavigationBar />
            <div ref={divRef} key={"map-wrapper-key"} className="map-wrapper">
                <canvas id="map-canvas" ref={canvasRef} style={{width: "100%", height: "100%", objectFit: "contain"}} />
                <MapWrapper divRef={divRef} canvasRef={canvasRef} searchId={parseInt(id)} />
            </div>
        </>
    );
}