import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import apiClient from "../api/apiClient";
import "./FindRecipes.css";

const TOP_TABS = [
  { key: "breakfast", label: "Breakfast", category: "Breakfast" },
  { key: "lunch", label: "Chicken", category: "Chicken" },
  { key: "dinner", label: "Beef", category: "Beef" },
  { key: "vegetarian", label: "Vegetarian", category: "Vegetarian" },
  { key: "vegan", label: "Vegan", category: "Vegan" },
];

const PERMANENT_ROWS = [
  { key: "highprotein", title: "High Protein", type: "ingredient", value: "Chicken" },
  { key: "seafood", title: "Seafood", type: "category", value: "Seafood" },
];

function Tile({ item, onClick }) {
  return (
    <button className="recipesTile" type="button" onClick={onClick}>
      <div className="recipesTileImgWrap" aria-hidden="true">
        <img className="recipesTileImg" src={item.thumbUrl} alt="" />
      </div>
      <div className="recipesTileName">{item.name}</div>
      <div className="recipesTileMeta">Tap for details</div>
    </button>
  );
}

function Row({ title, items, onSelect }) {
  return (
    <div className="recipesRow">
      <div className="recipesRowHead">
        <div className="recipesRowTitle">{title}</div>
        <button className="recipesSeeAll" type="button">
          See All
        </button>
      </div>

      <div className="recipesHScroll">
        {items.map((x) => (
          <Tile key={x.mealId} item={x} onClick={() => onSelect(x.mealId)} />
        ))}
      </div>
    </div>
  );
}

export default function FindRecipes() {
  const navigate = useNavigate();

  const [tab, setTab] = useState("dinner");

  const [tabItems, setTabItems] = useState([]);
  const [popular, setPopular] = useState(null);

  const [rowData, setRowData] = useState(() => {
    const init = {};
    PERMANENT_ROWS.forEach((r) => (init[r.key] = []));
    return init;
  });

  const [searchOpen, setSearchOpen] = useState(false);
  const [searchQ, setSearchQ] = useState("");
  const [searchResults, setSearchResults] = useState([]);
  const [searchLoading, setSearchLoading] = useState(false);

  const [detailOpen, setDetailOpen] = useState(false);
  const [detailLoading, setDetailLoading] = useState(false);
  const [detail, setDetail] = useState(null);

  useEffect(() => {
    if (!searchOpen && !detailOpen) return;
    const prev = document.body.style.overflow;
    document.body.style.overflow = "hidden";
    return () => {
      document.body.style.overflow = prev;
    };
  }, [searchOpen, detailOpen]);

  useEffect(() => {
    let cancelled = false;

    async function loadTabRow() {
      try {
        const t = TOP_TABS.find((x) => x.key === tab) || TOP_TABS[2];
        const res = await apiClient.get(`/api/recipes/category/${encodeURIComponent(t.category)}`);
        const items = Array.isArray(res.data) ? res.data.slice(0, 14) : [];
        if (!cancelled) setTabItems(items);
      } catch {
        if (!cancelled) setTabItems([]);
      }
    }

    loadTabRow();

    return () => {
      cancelled = true;
    };
  }, [tab]);

  useEffect(() => {
    let cancelled = false;

    async function loadPopular() {
      try {
        const res = await apiClient.get("/api/recipes/random");
        const first = Array.isArray(res.data) && res.data.length > 0 ? res.data[0] : null;
        if (!cancelled) setPopular(first);
      } catch {
        if (!cancelled) setPopular(null);
      }
    }

    loadPopular();

    return () => {
      cancelled = true;
    };
  }, []);

  useEffect(() => {
    let cancelled = false;

    async function loadPermanentRows() {
      try {
        const promises = PERMANENT_ROWS.map(async (r) => {
          const url =
            r.type === "category"
              ? `/api/recipes/category/${encodeURIComponent(r.value)}`
              : `/api/recipes/ingredient/${encodeURIComponent(r.value)}`;

          const res = await apiClient.get(url);
          return { key: r.key, items: Array.isArray(res.data) ? res.data.slice(0, 12) : [] };
        });

        const results = await Promise.all(promises);

        if (!cancelled) {
          const next = {};
          results.forEach((x) => (next[x.key] = x.items));
          setRowData(next);
        }
      } catch {
        if (!cancelled) {
          const next = {};
          PERMANENT_ROWS.forEach((r) => (next[r.key] = []));
          setRowData(next);
        }
      }
    }

    loadPermanentRows();

    return () => {
      cancelled = true;
    };
  }, []);

  async function openDetail(mealId) {
    setDetailOpen(true);
    setDetailLoading(true);
    setDetail(null);

    try {
      const res = await apiClient.get(`/api/recipes/${mealId}`);
      setDetail(res.data || null);
    } catch {
      setDetail(null);
    } finally {
      setDetailLoading(false);
    }
  }

  async function runSearch() {
    const q = searchQ.trim();
    if (!q) return;

    setSearchLoading(true);
    setSearchResults([]);

    try {
      const res = await apiClient.get("/api/recipes/search", { params: { q } });
      setSearchResults(Array.isArray(res.data) ? res.data : []);
    } catch {
      setSearchResults([]);
    } finally {
      setSearchLoading(false);
    }
  }

  const activeTab = TOP_TABS.find((x) => x.key === tab) || TOP_TABS[2];

  return (
    <div className="pageShell">
      <div className="recipesTopBar">
        <button className="recipesBackBtn" type="button" onClick={() => navigate("/food")}>
          Back
        </button>

        <div className="recipesTitle">Find Recipes</div>

        <button className="recipesSearchIcon" type="button" aria-label="Search" onClick={() => setSearchOpen(true)}>
          🔍
        </button>
      </div>

      <div className="pageBody recipesBody">
        <div className="recipesTabs">
          {TOP_TABS.map((t) => (
            <button
              key={t.key}
              type="button"
              className={tab === t.key ? "recipesTab isActive" : "recipesTab"}
              onClick={() => setTab(t.key)}
            >
              {t.label}
            </button>
          ))}
        </div>

        <div className="recipesDishCard">
          <div className="recipesDishLeft">
            <div className="recipesDishTitle">Popular Dish of the day</div>
            <div className="recipesDishLabel">Name</div>
            <div className="recipesDishValue">{popular ? popular.name : "Loading…"}</div>

            <div className="recipesDishLabel">Description</div>
            <div className="recipesDishValueSmall">Tap the image to view full recipe details.</div>
          </div>

          <button
            type="button"
            className="recipesDishImgBtn"
            onClick={() => {
              if (popular) openDetail(popular.mealId);
            }}
            aria-label="Open popular recipe"
          >
            <div className="recipesDishImgWrap">
              {popular ? <img className="recipesDishImg" src={popular.thumbUrl} alt="" /> : null}
            </div>
          </button>
        </div>

        <Row title={activeTab.category} items={tabItems} onSelect={openDetail} />
        <Row title={PERMANENT_ROWS[0].title} items={rowData.highprotein || []} onSelect={openDetail} />
        <Row title={PERMANENT_ROWS[1].title} items={rowData.seafood || []} onSelect={openDetail} />
      </div>

      {searchOpen ? (
        <div
          className="recipesOverlay"
          role="dialog"
          aria-modal="true"
          onMouseDown={(e) => {
            if (e.target === e.currentTarget) setSearchOpen(false);
          }}
        >
          <div className="recipesModal" onMouseDown={(e) => e.stopPropagation()}>
            <div className="recipesModalTitle">Search</div>
            <div className="recipesModalSub">Searching for: {searchQ.trim() ? `"${searchQ.trim()}"` : "—"}</div>

            <div className="recipesSearchRow">
              <input
                className="recipesSearchInput"
                value={searchQ}
                onChange={(e) => setSearchQ(e.target.value)}
                placeholder="Search recipes…"
                autoFocus
              />
              <button className="recipesSearchBtn" type="button" onClick={runSearch} disabled={searchLoading}>
                {searchLoading ? "…" : "Go"}
              </button>
            </div>

            <div className="recipesSearchResults">
              {searchLoading ? (
                <div className="recipesHint">Loading results…</div>
              ) : searchResults.length === 0 ? (
                <div className="recipesHint">No results yet. Try “chicken”, “pasta”, “salad”.</div>
              ) : (
                searchResults.map((x) => (
                  <button
                    key={x.mealId}
                    className="recipesResultRow"
                    type="button"
                    onClick={() => {
                      setSearchOpen(false);
                      openDetail(x.mealId);
                    }}
                  >
                    <img className="recipesResultImg" src={x.thumbUrl} alt="" />
                    <div className="recipesResultText">
                      <div className="recipesResultName">{x.name}</div>
                      <div className="recipesResultMeta">Tap to open</div>
                    </div>
                  </button>
                ))
              )}
            </div>

            <button className="recipesClose" type="button" onClick={() => setSearchOpen(false)}>
              Close
            </button>
          </div>
        </div>
      ) : null}

      {detailOpen ? (
        <div
          className="recipesOverlay"
          role="dialog"
          aria-modal="true"
          onMouseDown={(e) => {
            if (e.target === e.currentTarget) {
              setDetailOpen(false);
              setDetail(null);
            }
          }}
        >
          <div className="recipesDetailModal" onMouseDown={(e) => e.stopPropagation()}>
            {detailLoading ? (
              <div className="recipesHint">Loading recipe…</div>
            ) : !detail ? (
              <div className="recipesHint">Could not load recipe.</div>
            ) : (
              <>
                <div className="recipesDetailTop">
                  <div className="recipesDetailTitle">{detail.name}</div>
                  <div className="recipesDetailMeta">
                    {detail.area || "—"} <span className="dot">•</span> {detail.category || "—"}
                  </div>
                </div>

                <div className="recipesDetailImgWrap">
                  <img className="recipesDetailImg" src={detail.thumbUrl} alt="" />
                </div>

                <div className="recipesDetailSectionTitle">Ingredients</div>
                <div className="recipesIngList">
                  {Array.isArray(detail.ingredients) && detail.ingredients.length > 0 ? (
                    detail.ingredients.map((i, idx) => (
                      <div className="recipesIngRow" key={idx}>
                        <div className="recipesIngName">{i.ingredient}</div>
                        <div className="recipesIngMeasure">{i.measure}</div>
                      </div>
                    ))
                  ) : (
                    <div className="recipesHint">No ingredients listed.</div>
                  )}
                </div>

                <div className="recipesDetailSectionTitle">Instructions</div>
                <div className="recipesInstructions">{detail.instructions || "—"}</div>
              </>
            )}

            <button
              className="recipesClose"
              type="button"
              onClick={() => {
                setDetailOpen(false);
                setDetail(null);
              }}
            >
              Close
            </button>
          </div>
        </div>
      ) : null}
    </div>
  );
}
