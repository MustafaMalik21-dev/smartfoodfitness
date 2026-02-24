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

function Tile({ item, onClick }) { // Component that renders a single recipe tile with an image, name, and meta information, and triggers a callback when clicked to open the recipe details
  return (
    <button className="recipesTile" type="button" onClick={onClick} aria-label={`Open recipe: ${item.name}`}>
      <div className="recipesTileImgWrap" aria-hidden="true">
        <img className="recipesTileImg" src={item.thumbUrl} alt="" />
        <div className="recipesTileOverlay" aria-hidden="true">
          <div className="recipesTileCta">View</div>
        </div>
      </div>
      <div className="recipesTileName">{item.name}</div>
      <div className="recipesTileMeta">Tap to view recipe</div>
    </button>
  );
}

function Row({ title, items, onSelect }) { // Component that renders a horizontal scrollable row of recipe tiles for a specific category, with a title and a disabled "See All" button that indicates more recipes will be available in the future
  return (
    <div className="recipesRow">
      <div className="recipesRowHead">
        <div className="recipesRowTitle">{title}</div>
        <button className="recipesSeeAll" type="button" disabled aria-disabled="true" title="Coming soon">
          See All
        </button>
      </div>

      <div className="recipesHScroll" aria-label={`${title} recipes`}>
        {items.map((x) => (
          <Tile key={x.mealId} item={x} onClick={() => onSelect(x.mealId)} />
        ))}
      </div>
    </div>
  );
}

export default function FindRecipes() { // Main component for the Find Recipes page, responsible for fetching recipe data for different categories, managing state for loading, search functionality, and displaying recipe details in a modal when a recipe tile is clicked
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

  const modalOpen = searchOpen || detailOpen;

  useEffect(() => { // Effect hook that runs whenever a modal (search or detail) is open, preventing background scrolling by setting the body's overflow style to "hidden", and restoring it when the modal is closed
    if (!modalOpen) return;
    const prev = document.body.style.overflow;
    document.body.style.overflow = "hidden";
    return () => {
      document.body.style.overflow = prev;
    };
  }, [modalOpen]);

  useEffect(() => { // Effect hook that adds a keydown event listener to the window when a modal is open, allowing the user to close the modal by pressing the Escape key, and cleaning up the event listener when the modal is closed
    if (!modalOpen) return;
    function onKeyDown(e) {
      if (e.key === "Escape") {
        setSearchOpen(false);
        setDetailOpen(false);
        setDetail(null);
      }
    }
    window.addEventListener("keydown", onKeyDown);
    return () => window.removeEventListener("keydown", onKeyDown);
  }, [modalOpen]);

  useEffect(() => {
    let cancelled = false;

    async function loadTabRow() { // Effect hook that runs whenever the selected top tab changes, fetching the recipes for the selected category and updating the state with the new recipes, while handling cancellation to prevent state updates on unmounted components
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

  useEffect(() => { // Effect hook that runs on component mount to fetch a random popular recipe, updating the state with the fetched recipe, and handling cancellation to prevent state updates on unmounted components
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

    async function loadPermanentRows() { // Effect hook that runs on component mount to fetch the recipes for the permanent rows (High Protein and Seafood), making parallel API calls for each row, and updating the state with the fetched recipes, while handling cancellation to prevent state updates on unmounted components
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

  async function openDetail(mealId) { // Function that handles opening the recipe detail modal when a recipe tile is clicked, fetching the detailed information for the selected recipe and managing loading state for the detail view
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

  function closeSearch() {
    setSearchOpen(false);
  }

  function closeDetail() {
    setDetailOpen(false);
    setDetail(null);
  }

  const activeTab = TOP_TABS.find((x) => x.key === tab) || TOP_TABS[2];

  return ( // JSX for rendering the Find Recipes page, including the top bar with navigation and search button, the popular dish section, the category tabs, the recipe rows, and the modals for search and recipe details
    <div className={modalOpen ? "pageShell recipesShell isModalOpen" : "pageShell recipesShell"}>
      <div className="recipesTopBar">
        <button className="recipesBackBtn" type="button" onClick={() => navigate("/food")}>
          Back
        </button>

        <div className="recipesTitle">Find Recipes</div>

        <button className="recipesSearchIcon" type="button" aria-label="Search recipes" onClick={() => setSearchOpen(true)}>
          🔍
        </button>
      </div>

      <div className="pageBody recipesBody">
        <div className="recipesSectionHint">
          Tap any image to open the full recipe (ingredients + instructions).
        </div>

        <div className="recipesTabs" role="tablist" aria-label="Recipe categories">
          {TOP_TABS.map((t) => (
            <button
              key={t.key}
              type="button"
              role="tab"
              aria-selected={tab === t.key}
              className={tab === t.key ? "recipesTab isActive" : "recipesTab"}
              onClick={() => setTab(t.key)}
            >
              {t.label}
            </button>
          ))}
        </div>

        <div className="recipesDishCard">
          <div className="recipesDishLeft">
            <div className="recipesDishTitle">Popular dish</div>
            <div className="recipesDishLabel">Name</div>
            <div className="recipesDishValue">{popular ? popular.name : "Loading…"}</div>

            <div className="recipesDishLabel">Tip</div>
            <div className="recipesDishValueSmall">Tap “View recipe” to open details.</div>
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
              <div className="recipesDishOverlay" aria-hidden="true">
                <div className="recipesDishCta">View recipe</div>
              </div>
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
          aria-label="Search recipes"
          onMouseDown={(e) => {
            if (e.target === e.currentTarget) closeSearch();
          }}
        >
          <div className="recipesCard" onMouseDown={(e) => e.stopPropagation()}>
            <div className="recipesCardHead">
              <button className="recipesX" type="button" aria-label="Close" onClick={closeSearch}>
                ✕
              </button>
              <div className="recipesCardTitle">Search</div>
              <div className="recipesHeadSpacer" />
            </div>

            <div className="recipesCardBody">
              <div className="recipesModalSub">
                Type a keyword (e.g. “chicken”, “pasta”, “salad”) then press Go.
              </div>

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
                  <div className="recipesHint">Searching…</div>
                ) : searchResults.length === 0 ? (
                  <div className="recipesHint">No results yet — try a different keyword.</div>
                ) : (
                  searchResults.map((x) => (
                    <button
                      key={x.mealId}
                      className="recipesResultRow"
                      type="button"
                      onClick={() => {
                        closeSearch();
                        openDetail(x.mealId);
                      }}
                    >
                      <img className="recipesResultImg" src={x.thumbUrl} alt="" />
                      <div className="recipesResultText">
                        <div className="recipesResultName">{x.name}</div>
                        <div className="recipesResultMeta">Tap to open recipe</div>
                      </div>
                    </button>
                  ))
                )}
              </div>
            </div>

            <div className="recipesCardFoot">
              <button className="recipesClose" type="button" onClick={closeSearch}>
                Close
              </button>
            </div>
          </div>
        </div>
      ) : null}

      {detailOpen ? (
        <div
          className="recipesOverlay"
          role="dialog"
          aria-modal="true"
          aria-label="Recipe details"
          onMouseDown={(e) => {
            if (e.target === e.currentTarget) closeDetail();
          }}
        >
          <div className="recipesCard" onMouseDown={(e) => e.stopPropagation()}>
            <div className="recipesCardHead">
              <button className="recipesX" type="button" aria-label="Close" onClick={closeDetail}>
                ✕
              </button>
              <div className="recipesCardTitle">{detail ? detail.name : "Recipe"}</div>
              <div className="recipesHeadSpacer" />
            </div>

            <div className="recipesCardBody">
              {detailLoading ? (
                <div className="recipesHint">Loading recipe…</div>
              ) : !detail ? (
                <div className="recipesHint">Could not load recipe.</div>
              ) : (
                <>
                  <div className="recipesDetailMetaLine">
                    {detail.area || "—"} <span className="dot">•</span> {detail.category || "—"}
                  </div>

                  <div className="recipesDetailImgWrap">
                    <img className="recipesDetailImg" src={detail.thumbUrl} alt="" />
                    <div className="recipesDetailImgTag" aria-hidden="true">
                      Full recipe
                    </div>
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
            </div>

            <div className="recipesCardFoot">
              <button className="recipesClose" type="button" onClick={closeDetail}>
                Close
              </button>
            </div>
          </div>
        </div>
      ) : null}
    </div>
  );
}