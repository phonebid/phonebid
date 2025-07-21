import { apiClient } from "./apiClient";
import type {
  Auction,
  CreateAuctionRequest,
  Bid,
  PlaceBidRequest,
} from "types/AuctionTypes";

class AuctionService {
  private readonly baseURL = "/auctions";

  async getAuctions(): Promise<Auction[]> {
    return await apiClient.get<Auction[]>(this.baseURL);
  }

  async getAuction(id: number): Promise<Auction> {
    return await apiClient.get<Auction>(`${this.baseURL}/${id}`);
  }

  async createAuction(auction: CreateAuctionRequest): Promise<Auction> {
    return await apiClient.post<Auction>(this.baseURL, auction);
  }

  async getAuctionBids(auctionId: number): Promise<Bid[]> {
    return await apiClient.get<Bid[]>(`${this.baseURL}/${auctionId}/bids`);
  }

  async placeBid(auctionId: number, bid: PlaceBidRequest): Promise<Bid> {
    return await apiClient.post<Bid>(`${this.baseURL}/${auctionId}/bids`, bid);
  }

  async deleteAuction(id: number): Promise<void> {
    return await apiClient.delete<void>(`${this.baseURL}/${id}`);
  }
}

export const auctionService = new AuctionService();
