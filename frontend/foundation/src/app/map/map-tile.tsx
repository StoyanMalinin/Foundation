"use client";

import { useEffect, useState } from "react";
import * as tomtom from './tomtom-map-utils'
import { Tile } from "./map-utils";

type MapTileProps = {
    canvasCtx: CanvasRenderingContext2D | null,
    tile: Tile,

    lat: [number, number],
    lon: [number, number],
    drawWidth: number,
    drawHeight: number,
}

export default function MapTile(props: MapTileProps) {
    const [loading, setLoading] = useState<boolean>(true);

    useEffect(() => {
        const f = async function() {
            await fetchMapTile(props.tile);
            setLoading(false);
        };

        f();
    }, [loading]);

    if (props.canvasCtx) {
        const box = tomtom.tileZXYToLatLonBBox(props.tile.z, props.tile.x, props.tile.y);
        const latBox = [box[2], box[0]];   
        const lonBox = [box[1], box[3]];   
    
        const viewLatBox = [Math.max(latBox[0], props.lat[0]), Math.min(latBox[1], props.lat[1])];
        const viewLonBox = [Math.max(lonBox[0], props.lon[0]), Math.min(lonBox[1], props.lon[1])];

        const dx1Fraction = (viewLonBox[0] - props.lon[0]) / (props.lon[1] - props.lon[0]); // fix later when we support going around the world (and the others below)
        const dx2Fraction = (viewLonBox[1] - props.lon[0]) / (props.lon[1] - props.lon[0]);
        const dy1Fraction = (props.lat[1] - viewLatBox[1]) / (props.lat[1] - props.lat[0]); 
        const dy2Fraction = (props.lat[1] - viewLatBox[0]) / (props.lat[1] - props.lat[0]);
        const dWidthFraction = dx2Fraction - dx1Fraction; 
        const dHeightFraction = dy2Fraction - dy1Fraction; 
        
        const sx1Fraction = (viewLonBox[0] - lonBox[0]) / (lonBox[1] - lonBox[0]);
        const sx2Fraction = (viewLonBox[1] - lonBox[0]) / (lonBox[1] - lonBox[0]);
        const sy1Fraction = (latBox[1] - viewLatBox[1]) / (latBox[1] - latBox[0]); 
        const sy2Fraction = (latBox[1] - viewLatBox[0]) / (latBox[1] - latBox[0]);
        const sWidthFraction = sx2Fraction - sx1Fraction; 
        const sHeightFraction = sy2Fraction - sy1Fraction; 

        const sx = sx1Fraction * 256; // pls fix this
        const sy = sy1Fraction * 256;
        const dx = dx1Fraction * props.drawWidth;
        const dy = dy1Fraction * props.drawHeight;
        const sWidth = sWidthFraction * 256;
        const sHeight = sHeightFraction * 256;
        const dWidth = dWidthFraction * props.drawWidth;
        const dHeight = dHeightFraction * props.drawHeight;
        
        if (!loading) {
            props.canvasCtx.drawImage(getMapTile(props.tile), sx, sy, sWidth, sHeight, dx, dy, dWidth, dHeight);
        } else {
            props.canvasCtx.fillStyle = "white";
            props.canvasCtx.fillRect(dx, dy, dWidth, dHeight);
        }
        
    }

    return <></>
}

const tileCache: Record<string, HTMLImageElement> = {};

function checkMapTile(t: Tile): boolean {
    return tileCache[`${t.z},${t.x},${t.y}`] != undefined;
}

function getMapTile(t: Tile): HTMLImageElement {
    if (!checkMapTile(t)) {
        throw `Tile ${t.z},${t.x},${t.y} not found`;
    }
    return tileCache[`${t.z},${t.x},${t.y}`];
}

async function fetchMapTile(t: Tile): Promise<HTMLImageElement> {
    if (tileCache[`${t.z},${t.x},${t.y}`]) {
        return tileCache[`${t.z},${t.x},${t.y}`];
    }
    
    var url = `http://localhost:6969/map-tile?searchId=${1}&z=${t.z}&x=${t.x}&y=${t.y}`;

    try {
        const timerKey = `fetchMapTile-${Math.random()}`;

        console.time(timerKey);
        const response = await fetch(url);
        console.timeEnd(timerKey);

        if (response.status == 200) {
            const img = new Image();
            img.src = URL.createObjectURL(await response.blob());

            const p = new Promise<void>((resolve, reject) => {
                img.onload = () => resolve();
            });
            await p;
            
            tileCache[`${t.z},${t.x},${t.y}`] = img;

            return img;
        } else {
            throw response.statusText;
        }
    } catch (e) {
        console.error(e);
    }

    return new Image();
}