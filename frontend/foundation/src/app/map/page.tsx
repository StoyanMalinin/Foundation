"use client";

import { Suspense, useRef } from "react";
import MapWrapper from "./map-wrapper";
import { useSearchParams } from "next/navigation";
import { NavigationBar } from "../navigation/navigation-bar";

export default function Page() {
    return <MapPage />
}

function MapPage() {
    const searchParams = useSearchParams();
    const divRef = useRef<HTMLDivElement>(null) as React.RefObject<HTMLDivElement>;
    const canvasRef = useRef<HTMLCanvasElement>(null) as React.RefObject<HTMLCanvasElement>;

    const id = searchParams.get("id");
    if (id == null) return <p>You should specify search ID</p>;

    return (
        <>
            <NavigationBar />
            <div ref={divRef} key={"map-wrapper-key"} className="map-wrapper" style={{
                width: "95%",
                height: "80vh",
                margin: "auto",
            }}>
                <canvas id="map-canvas" ref={canvasRef} style={{
                    width: "100%", 
                    margin: 0,
                    border: "0.1vw solid black",
                    }} />
                <MapWrapper divRef={divRef} canvasRef={canvasRef} searchId={parseInt(id)} />
            </div>
        </>
    );
}