export interface CarritoItem {
  id: number;
  productoId: number;
  cantidad: number;
  precioUnitario: number;
  subtotal: number;
  nombre: string;
  imagenUrl: string;
  coleccionSet?: string;
}

export interface Carrito {
  id: number;
  usuarioId: number;
  estado: string;
  items: CarritoItem[];
}