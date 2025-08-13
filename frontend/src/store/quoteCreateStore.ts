import { create } from "zustand";
import { persist } from "zustand/middleware";
import type { QuoteDraft } from "types/QuoteTypes";

interface QuoteCreateState {
  step: number; // 1..4
  draft: QuoteDraft;
  setStep: (step: number) => void;
  updateDraft: (partial: Partial<QuoteDraft>) => void;
  reset: () => void;
}

export const useQuoteCreateStore = create<QuoteCreateState>()(
  persist(
    (set) => ({
      step: 1,
      draft: {},
      setStep: (step) => set({ step }),
      updateDraft: (partial) =>
        set((state) => ({ draft: { ...state.draft, ...partial } })),
      reset: () => set({ step: 1, draft: {} }),
    }),
    { name: "quote-create-draft" }
  )
);
