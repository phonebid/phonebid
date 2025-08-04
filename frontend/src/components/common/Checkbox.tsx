interface CheckboxProps {
  label: string;
  checked: boolean;
  onChange: (checked: boolean) => void;
  required?: boolean;
  disabled?: boolean;
  className?: string;
  error?: string;
}

const Checkbox: React.FC<CheckboxProps> = ({
  label,
  checked,
  onChange,
  required = false,
  disabled = false,
  className = "",
  error,
}) => {
  // 고유 ID 생성
  const checkboxId = `checkbox-${Math.random().toString(36).substr(2, 9)}`;

  const checkboxClasses = `
    w-4 h-4 text-indigo-600 bg-gray-100 border-gray-300 rounded
    focus:ring-indigo-500 focus:ring-2
    ${disabled ? "cursor-not-allowed opacity-50" : "cursor-pointer"}
    ${error ? "border-red-300" : "border-gray-300"}
  `.trim();

  const labelClasses = `
    ml-2 text-sm font-medium text-gray-700
    ${disabled ? "cursor-not-allowed opacity-50" : "cursor-pointer"}
  `.trim();

  return (
    <div className={`mb-4 ${className}`}>
      <div className="flex items-start">
        <input
          id={checkboxId}
          type="checkbox"
          className={checkboxClasses}
          checked={checked}
          onChange={(e) => onChange(e.target.checked)}
          disabled={disabled}
          required={required}
        />
        <label htmlFor={checkboxId} className={labelClasses}>
          {label}
          {required && <span className="text-red-500 ml-1">*</span>}
        </label>
      </div>
      {error && <p className="mt-1 text-sm text-red-600">{error}</p>}
    </div>
  );
};

export default Checkbox;
