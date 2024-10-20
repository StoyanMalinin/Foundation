"use client";

import MapTile from './map-tile';
import { boundingBoxToTileGrid } from './map-utils'

type MapProps = {
    lat: [number, number],
    lon: [number, number],
};  

export default function Map(props: MapProps) {
    const grid = boundingBoxToTileGrid(props.lat[0], props.lat[1], props.lon[0], props.lon[1]);
    
    const style: React.CSSProperties = {
        gridTemplateColumns: `repeat(${grid[0].length}, calc(100% / ${grid[0].length}))`, 
        gridTemplateRows: `repeat(${grid.length}, calc(100% / ${grid.length}))`,
        display: "grid",
        gap: "0",
        padding: "0px",
    }

    return <div style={style} key="map-key">
        {grid.flat().map((tile, ind) => <MapTile lat={props.lat} lon={props.lon} tile={tile} key={ind + 10} />)}
        </div>
}