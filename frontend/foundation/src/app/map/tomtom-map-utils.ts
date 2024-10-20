// Shamelessly copy-pasted from 
// https://developer.tomtom.com/map-display-api/documentation/zoom-levels-and-tile-grid

export const MIN_LAT = -85.051128779807
export const MAX_LAT = 85.051128779806
export const MIN_LON = -180.0
export const MAX_LON = 180.0
export const MIN_ZOOM_LEVEL = 0
export const MAX_ZOOM_LEVEL = 22

export function latLonToTileZXY(lat: number, lon: number, zoomLevel: number): [number, number, number] {
    if (
      zoomLevel == undefined ||
      isNaN(zoomLevel) ||
      zoomLevel < MIN_ZOOM_LEVEL ||
      zoomLevel > MAX_ZOOM_LEVEL
    ) {
      throw new Error(
        "Zoom level value is out of range [" +
          MIN_ZOOM_LEVEL.toString() +
          ", " +
          MAX_ZOOM_LEVEL.toString() +
          "]"
      )
    }
  
    if (lat == undefined || isNaN(lat) || lat < MIN_LAT || lat > MAX_LAT) {
      throw new Error(
        "Latitude value is out of range [" +
          MIN_LAT.toString() +
          ", " +
          MAX_LAT.toString() +
          "]"
      )
    }
  
    if (lon == undefined || isNaN(lon) || lon < MIN_LON || lon > MAX_LON) {
      throw new Error(
        "Longitude value is out of range [" +
          MIN_LON.toString() +
          ", " +
          MAX_LON.toString() +
          "]"
      )
    }
  
    let z = Math.trunc(zoomLevel)
    let xyTilesCount = Math.pow(2, z)
    let x = Math.trunc(Math.floor(((lon + 180.0) / 360.0) * xyTilesCount))
    let y = Math.trunc(
      Math.floor(
        ((1.0 -
          Math.log(
            Math.tan((lat * Math.PI) / 180.0) +
              1.0 / Math.cos((lat * Math.PI) / 180.0)
          ) /
            Math.PI) /
          2.0) *
          xyTilesCount
      )
    )
  
    return [z, x, y]
  }

  export function tileZXYToLatLon(zoomLevel: number, x: number, y: number): [number, number] {
    if (
      zoomLevel == undefined ||
      isNaN(zoomLevel) ||
      zoomLevel < MIN_ZOOM_LEVEL ||
      zoomLevel > MAX_ZOOM_LEVEL
    ) {
      throw new Error(
        "Zoom level value is out of range [" +
          MIN_ZOOM_LEVEL.toString() +
          "," +
          MAX_ZOOM_LEVEL.toString() +
          "]"
      )
    }
  
    let z = Math.trunc(zoomLevel)
    let minXY = 0
    let maxXY = Math.pow(2, z) - 1
  
    if (x == undefined || isNaN(x) || x < minXY || x > maxXY) {
      throw new Error(
        "Tile x value is out of range [" +
          minXY.toString() +
          "," +
          maxXY.toString() +
          "]"
      )
    }
  
    if (y == undefined || isNaN(y) || y < minXY || y > maxXY) {
      throw new Error(
        "Tile y value is out of range [" +
          minXY.toString() +
          "," +
          maxXY.toString() +
          "]"
      )
    }
  
    let lon = (x / Math.pow(2, z)) * 360.0 - 180.0
  
    let n = Math.PI - (2.0 * Math.PI * y) / Math.pow(2, z)
    let lat = (180.0 / Math.PI) * Math.atan(0.5 * (Math.exp(n) - Math.exp(-n)))
  
    return [lat, lon]
  }

  export function tileZXYToLatLonBBox(zoomLevel: number, x: number, y: number): [number, number, number, number] {
    if (
      zoomLevel == undefined ||
      isNaN(zoomLevel) ||
      zoomLevel < MIN_ZOOM_LEVEL ||
      zoomLevel > MAX_ZOOM_LEVEL
    ) {
      throw new Error(
        "Zoom level value is out of range [" +
          MIN_ZOOM_LEVEL.toString() +
          "," +
          MAX_ZOOM_LEVEL.toString() +
          "]"
      )
    }
  
    let z = Math.trunc(zoomLevel)
    let minXY = 0
    let maxXY = Math.pow(2, z) - 1
  
    if (x == undefined || isNaN(x) || x < minXY || x > maxXY) {
      throw new Error(
        "Tile x value is out of range [" +
          minXY.toString() +
          "," +
          maxXY.toString() +
          "]"
      )
    }
  
    if (y == undefined || isNaN(y) || y < minXY || y > maxXY) {
      throw new Error(
        "Tile y value is out of range [" +
          minXY.toString() +
          "," +
          maxXY.toString() +
          "]"
      )
    }
  
    let lon1 = (x / Math.pow(2, z)) * 360.0 - 180.0
  
    let n1 = Math.PI - (2.0 * Math.PI * y) / Math.pow(2, z)
    let lat1 = (180.0 / Math.PI) * Math.atan(0.5 * (Math.exp(n1) - Math.exp(-n1)))
  
    let lon2 = ((x + 1) / Math.pow(2, z)) * 360.0 - 180.0
  
    let n2 = Math.PI - (2.0 * Math.PI * (y + 1)) / Math.pow(2, z)
    let lat2 = (180.0 / Math.PI) * Math.atan(0.5 * (Math.exp(n2) - Math.exp(-n2)))
  
    return [lat1, lon1, lat2, lon2]
  }