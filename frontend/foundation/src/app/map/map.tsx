"use client";

import { useEffect, useState } from "react";
import { boundingBoxToTileGrid } from './map-utils'

type MapProps = {
    lat: [number, number],
    lon: [number, number],
};  

export default function Map(props: MapProps) {
    const grid = boundingBoxToTileGrid(props.lat[0], props.lat[1], props.lon[0], props.lon[1]);
    
    return <div style={{gridTemplateColumns: `repeat(${grid[0].length}, 1fr)`, display: "grid"}}>
        {grid.flat().map((tile, ind) => <div key={ind}>{ind}</div>)}
        </div>
}

async function getMapRegion(lat: [number, number], lon: [number, number]): Promise<string> {
    var url = `http://localhost:6969/map?searchId=${1}&lat=${(lat[0] + lat[1]) / 2}&lon=${(lon[0] + lon[1]) / 2}&sz=${ (lat[1] - lat[0]) * 30000}`;

    const response = await fetch(url);
    if (response.status == 200) {
        return URL.createObjectURL(await response.blob());
    } else {
        throw response.statusText;
    }
}