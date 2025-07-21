export interface Auction {
  id: number;
  product: string;
  model: string;
  capacity: string;
  color: string;
  carrier: string;
  condition: string;
  hopePrice: number;
  endTime: string;
  status: "ACTIVE" | "COMPLETED" | "CANCELLED";
  createdAt: string;
  updatedAt: string;
}

export interface CreateAuctionRequest {
  product: string;
  model: string;
  capacity: string;
  color: string;
  carrier: string;
  condition: string;
  hopePrice: number;
}

export interface Bid {
  id: number;
  auctionId: number;
  sellerId: number;
  sellerName: string;
  price: number;
  message: string;
  estimatedDelivery: string;
  createdAt: string;
}

export interface PlaceBidRequest {
  price: number;
  message: string;
  estimatedDelivery: string;
}
