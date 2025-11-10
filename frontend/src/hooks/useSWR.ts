export { mutate } from "swr";

export const invalidatePattern = async (pattern: string): Promise<void> => {
  const { mutate } = await import("swr");
  await mutate(
    (key) => typeof key === "string" && key.includes(pattern),
    undefined,
    { revalidate: true }
  );
};
