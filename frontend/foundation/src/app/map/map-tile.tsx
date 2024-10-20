"use client"

import { useEffect, useMemo, useState } from 'react';
import * as utils from './map-utils'

type MapProps = {
    lat: [number, number],
    lon: [number, number],
    tile: utils.Tile,
};  

export default function MapTile(props: MapProps) {
    const [tileURL, setTileURL] = useState<string | null>(null);

    useEffect(() => {
            setTileURL(null);

        try {
            const f = async function() {
                if (tileCache[JSON.stringify(props.tile)]) {
                    setTileURL(tileCache[JSON.stringify(props.tile)]);
                } else {
                    const url = await fetchMapTile(props.tile.z, props.tile.x, props.tile.y);
                    
                    tileCache[JSON.stringify(props.tile)] = url;
                    setTileURL(url);
                }
            }

            f();
        } catch(e) {
            console.log(props.tile, e);
        }
            
    }, [props]);

    if (tileURL == null) {
        return <>Loading</>
    } else {
        const style: React.CSSProperties = {
            height: `100%`,
            width: `100%`,
            objectFit: `contain`,
            pointerEvents: 'none',
            userSelect: 'none',
            
        }
        return <><img src={tileURL} key={JSON.stringify(props.tile)} unselectable='on' style={style}></img></>
    }
}

async function fetchMapTile(z: number, x: number, y: number): Promise<string> {
    var url = `http://localhost:6969/map-tile?searchId=${1}&z=${z}&x=${x}&y=${y}`;

    const response = await fetch(url);
    if (response.status == 200) {
        return URL.createObjectURL(await response.blob());
    } else {
        throw response.statusText;
    }
}