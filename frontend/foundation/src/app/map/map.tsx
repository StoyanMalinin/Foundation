"use client";

import { useEffect, useState } from 'react';
import { boundingBoxToTileGrids } from './map-utils'
import MapTile from './map-tile';

type MapProps = {
    searchId: number,
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
    }, [props.canvasRef, props.drawHeight, props.drawWidth]);

    if (!canvasCtx) {
        return <></>;
    }

    const grids = boundingBoxToTileGrids(props.lat[0], props.lat[1], props.lon[0], props.lon[1]);

    return <>{
        grids.map((grid, i) => {
            let drawWidthOffset = 0;
            if (i == 1) {
                drawWidthOffset = props.drawWidth * (grids[0].boundingBox.lon[1] - grids[0].boundingBox.lon[0]) / (props.lon[1] - props.lon[0]);
            } else if (i > 1) {
                throw `unexpected grid index ${i}`;
            }

            const drawWidth = props.drawWidth * (grid.boundingBox.lon[1] - grid.boundingBox.lon[0]) / (props.lon[1] - props.lon[0]);

            return grid.grid.map(line =>
                line.map(tile =>
                    <MapTile key={`map-tile-${tile.z}-${tile.x}-${tile.y}-${grid.boundingBox.lat[0]}`} canvasCtx={canvasCtx} tile={tile} lat={props.lat} lon={grid.boundingBox.lon} drawWidth={drawWidth} drawHeight={props.drawHeight} drawWidthOffset={drawWidthOffset} searchId={props.searchId} />
                )
            )
        })
    }</>
}