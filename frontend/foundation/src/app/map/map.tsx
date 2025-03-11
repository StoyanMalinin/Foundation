"use client";

import { useEffect, useReducer, useState } from 'react';
import { boundingBoxToTileGrid, Tile } from './map-utils'
import MapTile from './map-tile';

type MapProps = {
    lat: [number, number],
    lon: [number, number],
    drawWidth: number,
    drawHeight: number,
    canvasRef: React.RefObject<HTMLCanvasElement>,
};  

export default function Map(props: MapProps) {
    const [canvasCtx, setCanvasCtx] = useState<CanvasRenderingContext2D | null>(null);
    useEffect(() => {
            if (props.canvasRef.current) {
                props.canvasRef.current.width = props.drawWidth;
                props.canvasRef.current.height = props.drawHeight;
                
                const _canvas = props.canvasRef.current;
                setCanvasCtx(_canvas.getContext("2d"));
            }
    }, [props.canvasRef, props.canvasRef.current]);

    const grid = boundingBoxToTileGrid(props.lat[0], props.lat[1], props.lon[0], props.lon[1]);

    return <>{
        grid.map(line =>
            line.map(tile =>
                <MapTile key={`map-tile-${tile.z}-${tile.x}-${tile.y}`} canvasCtx={canvasCtx} tile={tile} lat={props.lat} lon={props.lon} drawWidth={props.drawWidth} drawHeight={props.drawHeight} />
            )
        )
    }</>
}