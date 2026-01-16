export interface BidCalculationInput {
  devicePrice: number;
  publicSubsidy: number;
  additionalSubsidy: number;
  installmentMonths: number;
  installmentInterestRate: number;
}

export interface BidCalculationResult {
  installmentPrincipal: number;
  monthlyInstallment: number;
  totalMonthlyPayment: number;
}

export const calculateInstallmentPrincipal = (
  devicePrice: number,
  publicSubsidy: number,
  additionalSubsidy: number
): number => {
  return devicePrice - publicSubsidy - additionalSubsidy;
};

export const calculateMonthlyInstallment = (
  principal: number,
  months: number,
  interestRate: number
): number => {
  if (months === 0) return principal;
  
  const monthlyRate = interestRate / 100 / 12;
  const numerator = principal * monthlyRate * Math.pow(1 + monthlyRate, months);
  const denominator = Math.pow(1 + monthlyRate, months) - 1;
  
  return Math.round(numerator / denominator);
};

export const calculateTotalMonthlyPayment = (
  monthlyInstallment: number,
  pricePlanPrice: number,
  additionalServicesPrice: number = 0
): number => {
  return monthlyInstallment + pricePlanPrice + additionalServicesPrice;
};

export const calculateBidAmounts = (
  input: BidCalculationInput
): BidCalculationResult => {
  const installmentPrincipal = calculateInstallmentPrincipal(
    input.devicePrice,
    input.publicSubsidy,
    input.additionalSubsidy
  );

  const monthlyInstallment = calculateMonthlyInstallment(
    installmentPrincipal,
    input.installmentMonths,
    input.installmentInterestRate
  );

  return {
    installmentPrincipal,
    monthlyInstallment,
    totalMonthlyPayment: monthlyInstallment,
  };
};

