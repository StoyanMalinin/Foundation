"use client";

import { useEffect, useReducer, useRef, useState } from 'react';
import MapTile from './map-tile';
import { boundingBoxToTileGrid } from './map-utils'
import * as utils from './map-utils'
import * as tomtom from './tomtom-map-utils'

type MapProps = {
    lat: [number, number],
    lon: [number, number],
    drawWidth: number,
    drawHeight: number,
};  

const tiles: Record<string, string> = {};

export default function Map(props: MapProps) {
    const grid = boundingBoxToTileGrid(props.lat[0], props.lat[1], props.lon[0], props.lon[1]);
    const rows = grid.length;
    const cols = grid[0].length;
    const canvasRef = useRef(null);
    const [ctx, setCtx] = useState<CanvasRenderingContext2D | null>(null);
    const [canvas, setCanvas] = useState<HTMLCanvasElement | null>(null);
    const [, forceUpdate] = useReducer(x => x + 1, 0);

    useEffect(() => {
        if (canvasRef?.current) {
            const _canvas = canvasRef?.current as HTMLCanvasElement;
            setCtx(_canvas.getContext("2d"));
            setCanvas(_canvas);
        }
    }, []);

    useEffect(() => {
        for (let i = 0; i < rows; i++) {
            for (var j = 0; j < cols; j++) {
                const f = async function() {
                    if (!tiles[JSON.stringify(grid[i][j])]) {
                        tiles[JSON.stringify(grid[i][j])] = await fetchMapTile(grid[i][j].z, grid[i][j].x, grid[i][j].y);
                        forceUpdate();
                    }
                }

                f();
            }
        }
    })

    if (ctx && canvas) {
        ctx?.clearRect(0, 0, canvas.width, canvas.height);
        console.log("grid-dump", grid, rows, cols);

        for (let i = 0; i < rows; i++) {
            for (var j = 0; j < cols; j++) {
                if (!tiles[JSON.stringify(grid[i][j])]) {
                    continue;
                }

                if (!grid[i][j]) {
                    console.log("grid render", i, j, rows, cols);
                }
    
                const box = tomtom.tileZXYToLatLonBBox(grid[i][j].z, grid[i][j].x, grid[i][j].y);
                const latBox = [box[2], box[0]];   
                const lonBox = [box[1], box[3]];   
            
                const viewLatBox = [Math.max(latBox[0], props.lat[0]), Math.min(latBox[1], props.lat[1])];
                const viewLonBox = [Math.max(lonBox[0], props.lon[0]), Math.min(lonBox[1], props.lon[1])];
    
                const {width, height} = canvas.getBoundingClientRect();
    
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
                const dx = dx1Fraction * width;
                const dy = dy1Fraction * height;
                const sWidth = sWidthFraction * 256;
                const sHeight = sHeightFraction * 256;
                const dWidth = dWidthFraction * width;
                const dHeight = dHeightFraction * height;
            
                const ctx = canvas.getContext("2d");
    
                const img = new Image();
                img.src = tiles[JSON.stringify(grid[i][j])];

                console.log(cols, rows, dx1Fraction, dy1Fraction, sx1Fraction, sy1Fraction, props, lonBox);
                
                img.onload = () => ctx?.drawImage(img, sx, sy, sWidth, sHeight, dx, dy, dWidth, dHeight);
            }
        }
    }

    return <canvas id="map-canvas" ref={canvasRef} width={props.drawWidth} height={props.drawHeight} style={{width: "100%", height: "100%", objectFit: "contain"}}></canvas>
}

async function fetchMapTile(z: number, x: number, y: number): Promise<Image> {
    console.log("FETCHING", z, x, y);
    var url = `http://localhost:6969/map-tile?searchId=${1}&z=${z}&x=${x}&y=${y}`;

    const response = await fetch(url);
    if (response.status == 200) {
        return URL.createObjectURL(await response.blob());
    } else {
        throw response.statusText;
    }
}
