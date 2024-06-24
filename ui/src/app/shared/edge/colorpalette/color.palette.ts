//colorblind friendly palette see https://davidmathlogic.com/colorblind/#%23332288-%23117733-%2344AA99-%2388CCEE-%23DDCC77-%23CC6677-%23AA4499-%23882255
//palette by https://personal.sron.nl/~pault/
export enum ThermometerColors {

    BLUE = "#332288",
    GREEN = "#117733",
    TURQUOISE = "#44AA99",
    CYAN = "#88CCEE",
    YELLOW = "#DDCC77",
    PALE_RED = "#CC6677",
    PURPLE = "#AA4499",
    WHINE_RED = "#882255",
    GREY = "#BBBBBB"
}

export function getColorByIndex(index: number): ThermometerColors {
    const colors = Object.values(ThermometerColors);
    index = Math.abs(index);
    index = index % colors.length;
    return colors[index];
}

export function getColorByIndexRGB(index:number): string {

    const color = getColorByIndex(index);
     // Remove the '#' character if present
     const hexString = color.replace('#', '');

     // Parse the hexadecimal color string to RGB components
     const r = parseInt(hexString.substring(0, 2), 16);
     const g = parseInt(hexString.substring(2, 4), 16);
     const b = parseInt(hexString.substring(4, 6), 16);

     // Return the RGB string
     return `rgb(${r},${g},${b})`;
}

export function getColorByIndexRGBAndAddAlpha(index: number, alphaValue: number): string {
    const color = getColorByIndex(index);
    // Remove the '#' character if present
    const hexString = color.replace('#', '');

    // Parse the hexadecimal color string to RGB components
    const r = parseInt(hexString.substring(0, 2), 16);
    const g = parseInt(hexString.substring(2, 4), 16);
    const b = parseInt(hexString.substring(4, 6), 16);

    // Return the RGB string
    return `rgba(${r},${g},${b}, ${alphaValue})`;
}
