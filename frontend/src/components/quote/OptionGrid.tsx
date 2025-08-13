import React from "react";

export interface OptionItem {
  label: string;
  value: string;
  badge?: string;
}

interface OptionGridProps {
  items: OptionItem[];
  selectedValue?: string;
  onSelect: (value: string) => void;
  columns?: 2 | 3 | 4;
  ariaLabel?: string;
}

const colClassMap: Record<NonNullable<OptionGridProps["columns"]>, string> = {
  2: "grid-cols-2",
  3: "grid-cols-3",
  4: "grid-cols-4",
};

const OptionGrid: React.FC<OptionGridProps> = ({
  items,
  selectedValue,
  onSelect,
  columns = 3,
  ariaLabel,
}) => {
  const handleKey = (
    e: React.KeyboardEvent<HTMLButtonElement>,
    value: string
  ) => {
    if (e.key === "Enter" || e.key === " ") {
      e.preventDefault();
      onSelect(value);
    }
  };

  return (
    <div
      className={`grid ${colClassMap[columns]} gap-2`}
      role="listbox"
      aria-label={ariaLabel}
    >
      {items.map((item) => {
        const isSelected = item.value === selectedValue;
        return (
          <button
            key={item.value}
            type="button"
            role="option"
            aria-selected={isSelected}
            onClick={() => onSelect(item.value)}
            onKeyDown={(e) => handleKey(e, item.value)}
            className={[
              "h-10 px-3 rounded-lg border text-sm transition-colors",
              isSelected
                ? "border-primary-600 bg-primary-50 text-primary-700"
                : "border-input bg-white hover:bg-accent",
            ].join(" ")}
          >
            <span className="inline-flex items-center gap-1">
              <span className="truncate">{item.label}</span>
              {item.badge && (
                <span className="text-[10px] px-1.5 py-0.5 rounded bg-gray-100 text-gray-700 border border-gray-200">
                  {item.badge}
                </span>
              )}
            </span>
          </button>
        );
      })}
    </div>
  );
};

export default OptionGrid;

