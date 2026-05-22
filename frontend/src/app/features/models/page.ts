export interface Page<T> {
  content: T[];
  empty: boolean;
  first: boolean;
  last: boolean;
  size: number;
  number: number;
  numberOfElements: number;
  totalElements: number;
  totalPages: number;
}
