import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import apiClient from "../api/apiClient";
import { useAuth } from "../auth/useAuth";
import "./LogFood.css";

function n(v) {
  const x = Number(v);
  return Number.isFinite(x) ? x : 0;
}

function roundInt(v) {
  const x = Number(v);
  if (!Number.isFinite(x)) return 0;
  return Math.round(x);
}

function calcFromPer100(per100, grams) {
  return (n(per100) * n(grams)) / 100;
}

function clampNonNeg(v) {
  const x = Number(v);
  if (!Number.isFinite(x)) return 0;
  return Math.max(0, x);
}

export default function LogFood() {
  const navigate = useNavigate();
  const { auth } = useAuth();
  const userId = auth ? auth.userId : null;

  const [tab, setTab] = useState("Popular");
  const [q, setQ] = useState("");

  const [loading, setLoading] = useState(false);
  const [err, setErr] = useState("");
  const [items, setItems] = useState([]);

  const [recentLoading, setRecentLoading] = useState(false);
  const [recentErr, setRecentErr] = useState("");
  const [recentItems, setRecentItems] = useState([]);

  const [modalOpen, setModalOpen] = useState(false);
  const [selected, setSelected] = useState(null);
  const [grams, setGrams] = useState("100");
  const [saving, setSaving] = useState(false);

  const [manualOpen, setManualOpen] = useState(false);
  const [mName, setMName] = useState("");
  const [mGrams, setMGrams] = useState("100");
  const [mKcal, setMKcal] = useState("");
  const [mP, setMP] = useState("");
  const [mC, setMC] = useState("");
  const [mF, setMF] = useState("");
  const [manualSaving, setManualSaving] = useState(false);
  const [manualErr, setManualErr] = useState("");

  const query = useMemo(() => q.trim(), [q]);

  useEffect(() => {
    if (!modalOpen && !manualOpen) return;
    const prev = document.body.style.overflow;
    document.body.style.overflow = "hidden";
    return () => {
      document.body.style.overflow = prev;
    };
  }, [modalOpen, manualOpen]);

  useEffect(() => {
    let cancelled = false;

    async function runPopular() {
      setErr("");
      setLoading(true);
      try {
        const res = await apiClient.get("/api/food-database/search", {
          params: { q: query || "chicken", limit: 25 },
        });

        const next = res && res.data && res.data.items ? res.data.items : [];
        if (!cancelled) setItems(next);
      } catch {
        if (!cancelled) setErr("Failed to load foods.");
      } finally {
        if (!cancelled) setLoading(false);
      }
    }

    if (tab === "Popular") runPopular();
    else setItems([]);

    return () => {
      cancelled = true;
    };
  }, [query, tab]);

  useEffect(() => {
    let cancelled = false;

    async function loadRecent() {
      if (tab !== "Recent") return;

      if (!userId) {
        setRecentItems([]);
        setRecentErr("Log in to view your recent foods.");
        return;
      }

      setRecentErr("");
      setRecentLoading(true);

      try {
        const res = await apiClient.get(`/api/food-entry-logs/user/${userId}`);
        const list = Array.isArray(res.data) ? res.data : Array.isArray(res.data?.items) ? res.data.items : [];

        if (cancelled) return;

        const mapped = list.map((x, i) => {
          const weight = clampNonNeg(x?.weightValue);
          const kcal = clampNonNeg(x?.calories);
          const p = clampNonNeg(x?.proteins);
          const c = clampNonNeg(x?.carbs);
          const f = clampNonNeg(x?.fats);

          const per100 = weight > 0 ? (kcal / weight) * 100 : 0;

          return {
            key: String(x?.id ?? `${x?.foodName ?? "food"}-${i}`),
            name: String(x?.foodName || "Food item"),
            grams: roundInt(weight),
            kcal: roundInt(kcal),
            p: roundInt(p),
            c: roundInt(c),
            f: roundInt(f),
            per100Derived: {
              kcal: per100,
              p: weight > 0 ? (p / weight) * 100 : 0,
              c: weight > 0 ? (c / weight) * 100 : 0,
              f: weight > 0 ? (f / weight) * 100 : 0,
            },
          };
        });

        setRecentItems(mapped);
      } catch {
        if (!cancelled) {
          setRecentItems([]);
          setRecentErr("Could not load recent foods.");
        }
      } finally {
        if (!cancelled) setRecentLoading(false);
      }
    }

    loadRecent();

    return () => {
      cancelled = true;
    };
  }, [tab, userId]);

  const rows = useMemo(() => {
    if (tab !== "Popular") return [];

    return items.map((x) => {
      const display = x.brand ? `${x.name} (${x.brand})` : x.name;

      return {
        fdcId: x.fdcId,
        name: display || "Food item",
        per100: {
          kcal: x.kcalPer100g,
          p: x.proteinPer100g,
          c: x.carbsPer100g,
          f: x.fatPer100g,
        },
        imageUrl: x.imageUrl || null,
      };
    });
  }, [items, tab]);

  const gramsNum = useMemo(() => {
    const g = Number(String(grams).trim());
    if (!Number.isFinite(g)) return 0;
    if (g < 1) return 0;
    return g;
  }, [grams]);

  const modalTotals = useMemo(() => {
    if (!selected) return { kcal: 0, p: 0, c: 0, f: 0 };

    const kcal = calcFromPer100(selected.per100.kcal, gramsNum);
    const p = calcFromPer100(selected.per100.p, gramsNum);
    const c = calcFromPer100(selected.per100.c, gramsNum);
    const f = calcFromPer100(selected.per100.f, gramsNum);

    return {
      kcal: roundInt(kcal),
      p: roundInt(p),
      c: roundInt(c),
      f: roundInt(f),
    };
  }, [selected, gramsNum]);

  function openAdd(row) {
    setSelected(row);
    setGrams("100");
    setModalOpen(true);
    setErr("");
  }

  function openAddFromRecent(r) {
    const row = {
      fdcId: null,
      name: r.name,
      per100: {
        kcal: r.per100Derived.kcal,
        p: r.per100Derived.p,
        c: r.per100Derived.c,
        f: r.per100Derived.f,
      },
      imageUrl: null,
    };
    setSelected(row);
    setGrams(String(r.grams || 100));
    setModalOpen(true);
    setErr("");
  }

  function closeModal() {
    if (saving) return;
    setModalOpen(false);
    setSelected(null);
  }

  async function confirmAdd() {
    if (!userId) {
      navigate("/login");
      return;
    }
    if (!selected) return;
    if (gramsNum <= 0) {
      setErr("Please enter a valid weight.");
      return;
    }

    setSaving(true);
    setErr("");

    try {
      await apiClient.post("/api/food-entry-logs", {
        userId: userId,
        foodName: selected.name,
        weightValue: gramsNum,
        weightUnit: "g",
        calories: modalTotals.kcal,
        proteins: modalTotals.p,
        carbs: modalTotals.c,
        fats: modalTotals.f,
        mealType: null,
        loggedAt: new Date().toISOString(),
      });

      setModalOpen(false);
      setSelected(null);
      navigate("/food");
    } catch {
      setErr("Could not add food.");
    } finally {
      setSaving(false);
    }
  }

  function openManual() {
    setManualErr("");
    setMName("");
    setMGrams("100");
    setMKcal("");
    setMP("");
    setMC("");
    setMF("");
    setManualOpen(true);
  }

  function closeManual() {
    if (manualSaving) return;
    setManualOpen(false);
  }

  async function confirmManual() {
    if (!userId) {
      navigate("/login");
      return;
    }

    const foodName = String(mName || "").trim();
    const g = clampNonNeg(mGrams === "" ? 0 : mGrams);
    const kcal = clampNonNeg(mKcal === "" ? 0 : mKcal);
    const p = clampNonNeg(mP === "" ? 0 : mP);
    const c = clampNonNeg(mC === "" ? 0 : mC);
    const f = clampNonNeg(mF === "" ? 0 : mF);

    if (!foodName) {
      setManualErr("Please enter a food name.");
      return;
    }
    if (g <= 0) {
      setManualErr("Please enter a valid weight.");
      return;
    }

    setManualSaving(true);
    setManualErr("");

    try {
      await apiClient.post("/api/food-entry-logs", {
        userId: userId,
        foodName,
        weightValue: roundInt(g),
        weightUnit: "g",
        calories: roundInt(kcal),
        proteins: roundInt(p),
        carbs: roundInt(c),
        fats: roundInt(f),
        mealType: null,
        loggedAt: new Date().toISOString(),
      });

      setManualOpen(false);
      navigate("/food");
    } catch {
      setManualErr("Could not add food.");
    } finally {
      setManualSaving(false);
    }
  }

  const showEmpty = tab === "Popular" ? !loading && rows.length === 0 : tab === "Recent" ? !recentLoading && recentItems.length === 0 : false;

  return (
    <div className="pageShell">
      <div className="logFoodTop">
        <button className="logFoodBackBtn" type="button" onClick={() => navigate("/food")}>
          Back
        </button>
        <div className="logFoodTitle">Log Food</div>
        <div className="logFoodTopSpacer" />
      </div>

      <div className="pageBody logFoodBody">
        <div className="logFoodSearch">
          <span className="logFoodSearchIcon">🔍</span>
          <input
            className="logFoodSearchInput"
            value={q}
            onChange={(e) => setQ(e.target.value)}
            placeholder="Search for Foods"
          />
          <span className="logFoodMic">🎤</span>
        </div>

        <div className="logFoodTabs">
          {["Recent", "Popular"].map((t) => (
            <button
              key={t}
              type="button"
              className={`logFoodTab ${tab === t ? "isActive" : ""}`}
              onClick={() => setTab(t)}
            >
              {t}
            </button>
          ))}
        </div>

        {err ? <div className="logFoodInlineErr">{err}</div> : null}
        {recentErr && tab === "Recent" ? <div className="logFoodInlineErr">{recentErr}</div> : null}

        <div className="logFoodTable">
          <div className="logFoodHead">
            <div>Food Item</div>
            <div className="c">Weight / Portion</div>
            <div className="r">Calories (kcal)</div>
            <div />
          </div>

          <div className="logFoodRows">
            {tab === "Popular" ? (
              loading ? (
                <div className="logFoodEmpty">Loading…</div>
              ) : showEmpty ? (
                <div className="logFoodEmpty">No results</div>
              ) : (
                rows.map((x) => (
                  <div className="logFoodRow" key={String(x.fdcId || x.name)}>
                    <div className="cell nameCell">{x.name}</div>
                    <div className="cell c">100g</div>
                    <div className="cell r">{roundInt(x.per100.kcal) || "—"}</div>
                    <button className="addBtn" type="button" aria-label={`Add ${x.name}`} onClick={() => openAdd(x)}>
                      +
                    </button>
                  </div>
                ))
              )
            ) : recentLoading ? (
              <div className="logFoodEmpty">Loading…</div>
            ) : showEmpty ? (
              <div className="logFoodEmpty">No recent foods yet</div>
            ) : (
              recentItems.map((x) => (
                <div className="logFoodRow" key={x.key}>
                  <div className="cell nameCell">{x.name}</div>
                  <div className="cell c">{x.grams ? `${x.grams}g` : "—"}</div>
                  <div className="cell r">{x.kcal ? x.kcal : "—"}</div>
                  <button className="addBtn" type="button" aria-label={`Add ${x.name}`} onClick={() => openAddFromRecent(x)}>
                    +
                  </button>
                </div>
              ))
            )}
          </div>
        </div>

        <div className="logFoodActionsOne">
          <button className="logFoodActionBtn" type="button" onClick={openManual}>
            Manual Entry
          </button>
        </div>
      </div>

      {modalOpen && selected ? (
        <div className="lfModalOverlay" role="dialog" aria-modal="true" onMouseDown={closeModal}>
          <div className="lfModalCard" onMouseDown={(e) => e.stopPropagation()}>
            <div className="lfModalTop">
              <div className="lfModalTitle">Add food</div>
              <button className="lfModalClose" type="button" onClick={closeModal} aria-label="Close">
                ✕
              </button>
            </div>

            <div className="lfModalName">{selected.name}</div>

            <div className="lfModalGrid">
              <div className="lfModalImg">
                {selected.imageUrl ? <img src={selected.imageUrl} alt="" /> : <div className="lfModalImgPh">🍽️</div>}
              </div>

              <div className="lfModalForm">
                <div className="lfField">
                  <div className="lfLabel">Weight (g)</div>
                  <input
                    className="lfInput"
                    value={grams}
                    onChange={(e) => setGrams(e.target.value)}
                    inputMode="numeric"
                    placeholder="e.g. 150"
                  />
                </div>

                <div className="lfMacros">
                  <div className="lfMacroRow">
                    <span>Calories</span>
                    <strong>{modalTotals.kcal} kcal</strong>
                  </div>
                  <div className="lfMacroRow">
                    <span>Protein</span>
                    <strong>{modalTotals.p} g</strong>
                  </div>
                  <div className="lfMacroRow">
                    <span>Carbs</span>
                    <strong>{modalTotals.c} g</strong>
                  </div>
                  <div className="lfMacroRow">
                    <span>Fat</span>
                    <strong>{modalTotals.f} g</strong>
                  </div>
                </div>
              </div>
            </div>

            {err ? <div className="logFoodInlineErr" style={{ marginTop: 10 }}>{err}</div> : null}

            <div className="lfModalBtns">
              <button className="lfBtnSecondary" type="button" onClick={closeModal} disabled={saving}>
                Cancel
              </button>
              <button className="lfBtnPrimary" type="button" onClick={confirmAdd} disabled={saving}>
                {saving ? "Adding…" : "Add"}
              </button>
            </div>
          </div>
        </div>
      ) : null}

      {manualOpen ? (
        <div className="lfModalOverlay" role="dialog" aria-modal="true" onMouseDown={closeManual}>
          <div className="lfModalCard" onMouseDown={(e) => e.stopPropagation()}>
            <div className="lfModalTop">
              <div className="lfModalTitle">Manual entry</div>
              <button className="lfModalClose" type="button" onClick={closeManual} aria-label="Close">
                ✕
              </button>
            </div>

            {manualErr ? <div className="logFoodInlineErr">{manualErr}</div> : null}

            <div className="lfManualForm">
              <div className="lfField">
                <div className="lfLabel">Food name</div>
                <input
                  className="lfInput"
                  value={mName}
                  onChange={(e) => setMName(e.target.value)}
                  placeholder="e.g. Homemade chicken wrap"
                />
              </div>

              <div className="lfField">
                <div className="lfLabel">Weight (g)</div>
                <input
                  className="lfInput"
                  value={mGrams}
                  onChange={(e) => setMGrams(e.target.value)}
                  inputMode="numeric"
                  placeholder="e.g. 250"
                />
              </div>

              <div className="lfManualGrid">
                <div className="lfField">
                  <div className="lfLabel">Calories (kcal)</div>
                  <input className="lfInput" value={mKcal} onChange={(e) => setMKcal(e.target.value)} inputMode="numeric" placeholder="0" />
                </div>
                <div className="lfField">
                  <div className="lfLabel">Protein (g)</div>
                  <input className="lfInput" value={mP} onChange={(e) => setMP(e.target.value)} inputMode="numeric" placeholder="0" />
                </div>
                <div className="lfField">
                  <div className="lfLabel">Carbs (g)</div>
                  <input className="lfInput" value={mC} onChange={(e) => setMC(e.target.value)} inputMode="numeric" placeholder="0" />
                </div>
                <div className="lfField">
                  <div className="lfLabel">Fat (g)</div>
                  <input className="lfInput" value={mF} onChange={(e) => setMF(e.target.value)} inputMode="numeric" placeholder="0" />
                </div>
              </div>

              <div className="lfManualHint">
                Tip: leave macros empty if you don’t know them — they’ll be saved as 0.
              </div>
            </div>

            <div className="lfModalBtns">
              <button className="lfBtnSecondary" type="button" onClick={closeManual} disabled={manualSaving}>
                Cancel
              </button>
              <button className="lfBtnPrimary" type="button" onClick={confirmManual} disabled={manualSaving}>
                {manualSaving ? "Adding…" : "Add"}
              </button>
            </div>
          </div>
        </div>
      ) : null}
    </div>
  );
}
