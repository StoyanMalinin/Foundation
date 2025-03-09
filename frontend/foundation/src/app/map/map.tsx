"use client";

import { useEffect, useReducer, useRef, useState } from 'react';
import { boundingBoxToTileGrid } from './map-utils'
import * as tomtom from './tomtom-map-utils'

type MapProps = {
    lat: [number, number],
    lon: [number, number],
    drawWidth: number,
    drawHeight: number,
    canvasRef: React.RefObject<HTMLCanvasElement>,
};  

export default function Map(props: MapProps) {
    const [canvas, setCanvas] = useState<HTMLCanvasElement | null>(null);
    const [, forceUpdate] = useReducer(x => x + 1, 0);
    const [tiles, setTiles] = useState<Record<string, HTMLImageElement>>({});

    const grid = boundingBoxToTileGrid(props.lat[0], props.lat[1], props.lon[0], props.lon[1]);
    const rows = grid.length;
    const cols = grid[0].length;

    useEffect(() => {
        if (props.canvasRef.current) {
            console.log("Setting canvas");

            const _canvas = props.canvasRef.current;
            setCanvas(_canvas);
        }
    }, [props.canvasRef]);

    // Fix this shit:
    // 1. We essentially copy all the tiles into the `tiles` state property (maybe I don't use React properly)
    //   - We just need something to indicate which tiles are loaded and which are not 
    // 2. Too much async/await stuff - should be simpler (same argument about react as before)
    useEffect(() => {
        for (let i = 0; i < rows; i++) {
            for (var j = 0; j < cols; j++) {
                const f = async function() {
                    if (!tiles[JSON.stringify(grid[i][j])]) {
                        tiles[JSON.stringify(grid[i][j])] = await fetchMapTile(grid[i][j].z, grid[i][j].x, grid[i][j].y);
                        setTiles(tiles);
                    }
                };

                f();
            }
        }
    });

    if (props.canvasRef.current) {
        props.canvasRef.current.width = props.drawWidth;
        props.canvasRef.current.height = props.drawHeight;
    }

    if (canvas) {
        console.time("Render");

        const ctx = canvas.getContext("2d");
        if (ctx == null) {
            throw "Failed to get 2d context";
        }

        for (let i = 0; i < rows; i++) {
            for (var j = 0; j < cols; j++) {
                if (!tiles[JSON.stringify(grid[i][j])]) {
                    continue;
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
                
                ctx.drawImage(tiles[JSON.stringify(grid[i][j])], sx, sy, sWidth, sHeight, dx, dy, dWidth, dHeight);
            }
        }

        console.timeEnd("Render");
    }

    return <></>
}

const tileCache: Record<string, HTMLImageElement> = {};
async function fetchMapTile(z: number, x: number, y: number): Promise<HTMLImageElement> {
    if (tileCache[`${z},${x},${y}`]) {
        return tileCache[`${z},${x},${y}`];
    }
    
    var url = `http://localhost:6969/map-tile?searchId=${1}&z=${z}&x=${x}&y=${y}`;

    try {
        const response = await fetch(url);
        if (response.status == 200) {
            const img = new Image();
            img.src = URL.createObjectURL(await response.blob());

            const p = new Promise<void>((resolve, reject) => {
                img.onload = () => resolve();
            });
            await p;
            
            return img;
        } else {
            throw response.statusText;
        }
    } catch (e) {
        console.error(e);
    }

    return new Image();
}
