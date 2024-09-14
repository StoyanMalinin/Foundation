"use client";

import { useEffect, useRef, useState } from "react"

type MapProps = {
    lat: [number, number],
    lon: [number, number],
};

export default function Map(props: MapProps) {
    return <>{JSON.stringify(props.lat)} {JSON.stringify(props.lon)}</>;
}