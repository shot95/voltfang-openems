export namespace ArrayUtils {
  export function equalsCheck(a: any[], b: any[]) {
    return a.length === b.length &&
      a.every((v, i) => v === b[i]);
  }

  /**
   * Sort arrays alphabetically, according to the string returned by fn.
   * Elements for which fn returns null or undefined are sorted to the end in an undefined order.
   *
   * @param array to sort
   * @param fn to get a string to sort by
   * @returns sorted array
   */
  export function sortedAlphabetically<Type>(array: Type[], fn: (arg: Type) => string): Type[] {
    return array.sort((a: Type, b: Type) => {
      const aVal = fn(a);
      const bVal = fn(b);
      if (!aVal) {
        return !bVal ? 0 : 1;
      } else if (!bVal) {
        return -1;
      }
      return aVal.localeCompare(bVal, undefined, { sensitivity: 'accent' });
    });
  }
}
