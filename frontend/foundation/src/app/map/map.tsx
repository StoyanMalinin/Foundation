"use client";

import { useEffect, useState } from "react";

type MapProps = {
    lat: [number, number],
    lon: [number, number],
};  

export default function Map(props: MapProps) {
    const [mapURL, setMapURL] = useState<string>("");
    const [error, setError] = useState<string | null>(null);
    const [loading, setLoading] = useState<boolean>(true);

    useEffect(() => {
        async function f() {
            setLoading(true);

            try {
                setMapURL(await getMapRegion(props.lat, props.lon));
            } catch(e) {
                setError(JSON.stringify(e));
            }

            setLoading(false);
        }
        
        f();
    }, [props]);

    if (error != null) {
        return <p>Error {error}</p>
    } else if (loading) {
        return <p>Loading...</p>
    }
    
    return <img src={mapURL}></img>;
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