import { create } from "zustand";
import { devtools } from "zustand/middleware";
import type { Auction } from "types/AuctionTypes";

interface AuctionStore {
  // State
  auctions: Auction[];
  selectedAuction: Auction | null;
  loading: boolean;
  error: string | null;

  // Computed values (getters)
  activeAuctions: () => Auction[];
  completedAuctions: () => Auction[];
  cancelledAuctions: () => Auction[];
  auctionCounts: () => {
    total: number;
    active: number;
    completed: number;
    cancelled: number;
  };

  // Actions
  setAuctions: (auctions: Auction[]) => void;
  addAuction: (auction: Auction) => void;
  updateAuction: (id: number, updates: Partial<Auction>) => void;
  removeAuction: (id: number) => void;
  setSelectedAuction: (auction: Auction | null) => void;
  setLoading: (loading: boolean) => void;
  setError: (error: string | null) => void;
  clearError: () => void;
}

export const useAuctionStore = create<AuctionStore>()(
  devtools(
    (set, get) => ({
      // Initial state
      auctions: [],
      selectedAuction: null,
      loading: false,
      error: null,

      // Computed values (getters)
      activeAuctions: () => {
        return get().auctions.filter((auction) => auction.status === "ACTIVE");
      },

      completedAuctions: () => {
        return get().auctions.filter(
          (auction) => auction.status === "COMPLETED"
        );
      },

      cancelledAuctions: () => {
        return get().auctions.filter(
          (auction) => auction.status === "CANCELLED"
        );
      },

      auctionCounts: () => {
        const auctions = get().auctions;
        return {
          total: auctions.length,
          active: auctions.filter((a) => a.status === "ACTIVE").length,
          completed: auctions.filter((a) => a.status === "COMPLETED").length,
          cancelled: auctions.filter((a) => a.status === "CANCELLED").length,
        };
      },

      // Actions
      setAuctions: (auctions: Auction[]) => {
        set({ auctions }, false, "auction/setAuctions");
      },

      addAuction: (auction: Auction) => {
        set(
          (state) => ({
            auctions: [...state.auctions, auction],
          }),
          false,
          "auction/addAuction"
        );
      },

      updateAuction: (id: number, updates: Partial<Auction>) => {
        set(
          (state) => ({
            auctions: state.auctions.map((auction) =>
              auction.id === id ? { ...auction, ...updates } : auction
            ),
          }),
          false,
          "auction/updateAuction"
        );
      },

      removeAuction: (id: number) => {
        set(
          (state) => ({
            auctions: state.auctions.filter((auction) => auction.id !== id),
            selectedAuction:
              state.selectedAuction?.id === id ? null : state.selectedAuction,
          }),
          false,
          "auction/removeAuction"
        );
      },

      setSelectedAuction: (auction: Auction | null) => {
        set({ selectedAuction: auction }, false, "auction/setSelectedAuction");
      },

      setLoading: (loading: boolean) => {
        set({ loading }, false, "auction/setLoading");
      },

      setError: (error: string | null) => {
        set({ error }, false, "auction/setError");
      },

      clearError: () => {
        set({ error: null }, false, "auction/clearError");
      },
    }),
    {
      name: "auction-store",
    }
  )
);
