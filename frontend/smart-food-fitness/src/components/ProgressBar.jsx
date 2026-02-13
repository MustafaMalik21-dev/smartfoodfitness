export default function ProgressBar({ value, max }) {
  const safeMax = typeof max === "number" && max > 0 ? max : 0;
  const safeVal = typeof value === "number" && value >= 0 ? value : 0;

  const pct =
    safeMax > 0 ? Math.min(100, Math.round((safeVal / safeMax) * 100)) : 0;

  return (
    <div>
      {/* Track */}
      <div
        style={{
          height: 10,
          borderRadius: 999,
          background: "#e5e7eb",
          overflow: "hidden",
        }}
      >
        {/* Fill */}
        <div
          style={{
            height: "100%",
            width: `${pct}%`,
            background: "#0f766e",
          }}
        />
      </div>

      <div style={{ fontSize: 12, color: "#6b7280", marginTop: 6 }}>
        {pct}%
      </div>
    </div>
  );
}
