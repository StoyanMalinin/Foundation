import * as tomtom from "./tomtom-map-utils";

export type Tile = {
    z: number, 
    x: number, 
    y: number
};

export type LatLon = {
    lat: [number, number],
    lon: [number, number],
};

function clipBetween(x: number, l: number, r: number): number {
    return Math.min(Math.max(x, l), r);
}

export function normalizeBoundingBox(bb: LatLon): LatLon {
    // Latitude -> just clip, preserving the size
    if (bb.lat[0] < tomtom.MIN_LAT) {
        const diff = tomtom.MIN_LAT - bb.lat[0];
        bb.lat[0] = tomtom.MIN_LAT;
        bb.lat[1] = clipBetween(bb.lat[1] + diff, tomtom.MIN_LAT, tomtom.MAX_LAT);
    } else if (bb.lat[1] > tomtom.MAX_LAT) {
        const diff = bb.lat[1] - tomtom.MAX_LAT;
        bb.lat[1] = tomtom.MAX_LAT;
        bb.lat[0] = clipBetween(bb.lat[0] - diff, tomtom.MIN_LAT, tomtom.MAX_LAT);
    }

    // Longitude -> normalize if both boundries are outside the range
    if (bb.lon[1] < tomtom.MIN_LON) {
        bb.lon[0] += 360;
        bb.lon[1] += 360;
    } else if (bb.lon[0] > tomtom.MAX_LON) {
        bb.lon[0] -= 360;
        bb.lon[1] -= 360;
    }
    
    return bb;
}

export function recalculateAndNormalizeBoundingBox(scale: number, lat: [number, number], lon: [number, number], focus: [number, number]): LatLon {
    const newBoundingBox = recalculateBoundingBox(scale, lat, lon, focus);
    return normalizeBoundingBox(newBoundingBox);
}

// focus is where to zoom in/out on. focus is in [0, 1] x [0, 1]
export function recalculateBoundingBox(scale: number, lat: [number, number], lon: [number, number], focus: [number, number]): LatLon {    
    const latDelta = lat[1] - lat[0];
    const lonDelta = lon[1] - lon[0];
    
    const newLatDelta = clipBetween(latDelta * scale, 0.001, 180);
    const newLonDelta = clipBetween(lonDelta * scale, 0.001, 360);

    return {
        lat: [lat[0] + (latDelta - newLatDelta) * (1 - focus[1]), lat[1] - (latDelta - newLatDelta) * focus[1]],
        lon: [lon[0] + (lonDelta - newLonDelta) * focus[0], lon[1] - (lonDelta - newLonDelta) * (1 - focus[0])]
    }
}

function boundingBoxAndZoomLevelToGridSize(lat1: number, lat2: number, lons: number[], zoomLevel: number): number {
    if (lons.length % 2 != 0) {
        throw "lons should be even";
    }

    // the size can be either 2 or 4
    var total = 0;
    for (var i = 0; i < lons.length; i += 2) {
        total += simpleBoundingBoxToTileGridSize(lat1, lat2, lons[i], lons[i + 1], zoomLevel);
    }

    return total;
}

function simpleBoundingBoxToTileGridSize(lat1: number, lat2: number, lon1: number, lon2: number, zoomLevel: number): number {
    const [z1, minX, minY] = tomtom.latLonToTileZXY(lat2, lon1, zoomLevel);
    const [z2, maxX, maxY] = tomtom.latLonToTileZXY(lat1, lon2, zoomLevel);
    if (z1 != zoomLevel || z2 != zoomLevel) {
        throw `zoom levels should match ${z1} = ${zoomLevel}, ${z2} = ${zoomLevel}`;
    }

    return (maxY - minY + 1) * (maxX - minX + 1);
}

function getZoomLevelByBoundingBox(lat1: number, lat2: number, lons: number[]): number {
    const maxGridSize = 30;

    for (var z = tomtom.MAX_ZOOM_LEVEL; z >= tomtom.MIN_ZOOM_LEVEL; z--) {
        if (boundingBoxAndZoomLevelToGridSize(lat1, lat2, lons, z) <= maxGridSize) {
            return z;
        }
    }

    return -1;
}

type TileGrid = {
    grid: Tile[][],
    boundingBox: LatLon,
}

export function boundingBoxToTileGrids(lat1: number, lat2: number, lon1: number, lon2: number): TileGrid[] {    
    var lons;
    if (lon1 < tomtom.MIN_LON) {
        const diff = tomtom.MIN_LON - lon1;
        lons = [tomtom.MAX_LON - diff, tomtom.MAX_LON, tomtom.MIN_LON, lon2];
    } else if (lon2 > tomtom.MAX_LON) {
        const diff = lon2 - tomtom.MAX_LON;
        lons = [lon1, tomtom.MAX_LON, tomtom.MIN_LON, tomtom.MIN_LON + diff];
    } else {
        lons = [lon1, lon2];
    }

    const zoomLevel = getZoomLevelByBoundingBox(lat1, lat2, lons);

    var grids: TileGrid[] = [];

    for (var i = 0; i < lons.length; i += 2) {
        const grid: TileGrid = {grid: [], boundingBox: {lat: [0, 0], lon: [0, 0]}};
        grid.boundingBox = {lat: [lat1, lat2], lon: [lons[i], lons[i + 1]]};
        
        const [z1, minX, minY] = tomtom.latLonToTileZXY(lat2, lons[i], zoomLevel);
        const [z2, maxX, maxY] = tomtom.latLonToTileZXY(lat1, lons[i + 1], zoomLevel);
        if (z1 != zoomLevel || z2 != zoomLevel) {
            throw `zoom levels should match ${z1} = ${zoomLevel}, ${z2} = ${zoomLevel}`;
        }

        for (var y = minY; y <= maxY; y++) {
            grid.grid.push([]);
        }
 
        for (var y = minY; y <= maxY; y++) {
            for (var x = minX; x <= maxX; x++) {    
                grid.grid[y - minY].push({z: z1, x: x, y: y});
            }
        }

        grids.push(grid);
    }

    return grids;
}