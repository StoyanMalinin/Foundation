"use client";

import { useEffect, useState } from "react";
import * as tomtom from './tomtom-map-utils'
import { Tile } from "./map-utils";
import { FoundationBackend } from "@/backend/foundation-backend";

type MapTileProps = {
    searchId: number,

    canvasCtx: CanvasRenderingContext2D,
    tile: Tile,

    lat: [number, number],
    lon: [number, number],
    drawWidth: number,
    drawHeight: number,

    drawWidthOffset: number,
}

export default function MapTile(props: MapTileProps) {
    const [image, setImage] = useState<HTMLImageElement | null>(null);

    useEffect(() => {
        const f = async function() {
            const img = await fetchMapTile(props.tile, props.searchId);
            setImage(img);
        };

        f();
    }, [props.tile]);

    const box = tomtom.tileZXYToLatLonBBox(props.tile.z, props.tile.x, props.tile.y);
    const latBox = [box[2], box[0]];   
    const lonBox = [box[1], box[3]];   

    const viewLatBox = [Math.max(latBox[0], props.lat[0]), Math.min(latBox[1], props.lat[1])];
    const viewLonBox = [Math.max(lonBox[0], props.lon[0]), Math.min(lonBox[1], props.lon[1])];

    const dx1Fraction = (viewLonBox[0] - props.lon[0]) / (props.lon[1] - props.lon[0]);
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
    
    if (image != null) {
        props.canvasCtx.drawImage(image, sx, sy, sWidth, sHeight, dx + props.drawWidthOffset, dy, dWidth, dHeight);
    } else {
        props.canvasCtx.fillStyle = "white";
        props.canvasCtx.fillRect(dx, dy, dWidth, dHeight);
    }

    return <></>
}

const tileCache: Record<string, Promise<HTMLImageElement>> = {};

async function fetchMapTile(t: Tile, searchId: number): Promise<HTMLImageElement> {
    const key = `${t.z},${t.x},${t.y},${searchId}`;

    if (tileCache[key] !== undefined) {
        return tileCache[key];
    }
    
    console.time(`fetchMapTile ${key}`);
    tileCache[key] = FoundationBackend.getMapTile(searchId, t.z, t.x, t.y).then(response => {
        console.timeEnd(`fetchMapTile ${key}`);
        if (response.status == 200) {
            return response.blob();
        } else {
            throw response.statusText;
        }
    }).then(blob => {
        const img = new Image();
        img.src = URL.createObjectURL(blob);
        
        return img;
    }).then(img => {
        return new Promise<HTMLImageElement>((resolve, reject) => {
            img.onload = () => resolve(img);
            img.onerror = (event) => reject(event);
        });
    });

    return tileCache[key];
}