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

function boundingBoxAndZoomLevelToGridSize(lat1: number, lat2: number, lon1: number, lon2: number, zoomLevel: number): number {
    const [z1, minX, minY] = tomtom.latLonToTileZXY(lat2, lon1, zoomLevel);
    const [z2, maxX, maxY] = tomtom.latLonToTileZXY(lat1, lon2, zoomLevel);
    if (z1 != zoomLevel || z2 != zoomLevel) {
        throw `zoom levels should match ${z1} = ${zoomLevel}, ${z2} = ${zoomLevel}`;
    }

    return (maxY - minY + 1) * (maxX - minX + 1);
}

function getZoomLevelByBoundingBox(lat1: number, lat2: number, lon1: number, lon2: number): number {
    const maxGridSize = 30;

    for (var z = tomtom.MAX_ZOOM_LEVEL; z >= tomtom.MIN_ZOOM_LEVEL; z--) {
        if (boundingBoxAndZoomLevelToGridSize(lat1, lat2, lon1, lon2, z) <= maxGridSize) {
            return z;
        }
    }

    return -1;
}

export function boundingBoxToTileGrid(lat1: number, lat2: number, lon1: number, lon2: number): Tile[][] {
    var grid: Tile[][] = [];
    const zoomLevel = getZoomLevelByBoundingBox(lat1, lat2, lon1, lon2);
    
    var lons;
    if (lon1 < lon2) {
        lons = [lon1, lon2];
    } else {
        lons = [lon1, tomtom.MAX_LON, tomtom.MIN_LON, lon2];
    }

    for (var i = 0; i < lons.length; i += 2) {
        const [z1, minX, minY] = tomtom.latLonToTileZXY(lat2, lons[i], zoomLevel);
        const [z2, maxX, maxY] = tomtom.latLonToTileZXY(lat1, lons[i + 1], zoomLevel);
        if (z1 != zoomLevel || z2 != zoomLevel) {
            throw `zoom levels should match ${z1} = ${zoomLevel}, ${z2} = ${zoomLevel}`;
        }

        if (grid.length == 0) {
            for (var y = minY; y <= maxY; y++) {
                grid.push([]);
            }
        }
 
        for (var y = minY; y <= maxY; y++) {
            for (var x = minX; x <= maxX; x++) {    
                grid[y - minY].push({z: z1, x: x, y: y});
            }
        }
    }

    return grid;
}