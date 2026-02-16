import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import apiClient from "../api/apiClient";
import "./ExerciseEncyclopedia.css";
import "../styles/PageShell.css";

import ChestIcon from "../assets/Chesticon.png";
import CoreIcon from "../assets/Coreicon.png";
import LegsIcon from "../assets/Legsicon.png";
import ShoulderIcon from "../assets/Shouldericon.png";
import BackIcon from "../assets/Backicon.png";
import BicepIcon from "../assets/Bicepicon.png";
import CardioIcon from "../assets/Cardioicon.png";


function cleanHtmlToText(html) {
  if (!html) return "";
  return String(html).replace(/<[^>]*>/g, " ").replace(/\s+/g, " ").trim();
}

/* ===== CATEGORY PLACEHOLDER MAPPING ===== */
function placeholderForCategory(category) {
  const c = String(category || "").toLowerCase();

  if (c.includes("chest")) return ChestIcon;
  if (c.includes("back")) return BackIcon;
  if (c.includes("shoulder")) return ShoulderIcon;
  if (c.includes("arm")) return BicepIcon;
  if (c.includes("core")) return CoreIcon;
  if (c.includes("leg")) return LegsIcon;
  if (c.includes("cardio")) return CardioIcon; // cardio fallback

  return ChestIcon;
}

function bestImage(tile) {
  return tile?.imageUrl || placeholderForCategory(tile?.category);
}

/* ===== TILE ===== */
function Tile({ item, onClick }) {
  return (
    <button type="button" className="eeTile" onClick={onClick}>
      <div className="eeTileImg">
        <img className="eeTileImgEl" src={bestImage(item)} alt="" />
      </div>
      <div className="eeTileName">{item.name}</div>
      <div className="eeTileMeta">{item.category}</div>
    </button>
  );
}

/* ===== ROW ===== */
function Row({ title, items, onPick, onSeeAll }) {
  if (!items || items.length === 0) return null;

  return (
    <div className="eeRow">
      <div className="eeRowHead">
        <div className="eeRowTitle">{title}</div>
        <button className="eeSeeAll" type="button" onClick={onSeeAll}>
          See All
        </button>
      </div>

      <div className="eeHScroll">
        {items.map((x) => (
          <Tile key={x.id} item={x} onClick={() => onPick(x)} />
        ))}
      </div>
    </div>
  );
}

/* ===== MAIN COMPONENT ===== */
export default function ExerciseEncyclopedia() {
  const navigate = useNavigate();

  const [loading, setLoading] = useState(true);
  const [err, setErr] = useState("");
  const [data, setData] = useState(null);

  const [topFilter, setTopFilter] = useState("upper");
  const [searchOpen, setSearchOpen] = useState(false);
  const [q, setQ] = useState("");

  const [active, setActive] = useState(null);
  const [detail, setDetail] = useState(null);
  const [detailBusy, setDetailBusy] = useState(false);

  useEffect(() => {
    let cancelled = false;

    async function load() {
      try {
        setLoading(true);
        setErr("");
        const res = await apiClient.get("/api/exercises/encyclopedia", {
          params: { perCategoryLimit: 10 },
        });
        if (!cancelled) setData(res.data);
      } catch {
        if (!cancelled) setErr("Could not load exercises.");
      } finally {
        if (!cancelled) setLoading(false);
      }
    }

    load();
    return () => (cancelled = true);
  }, []);

  const groups = useMemo(() => {
    const d = data || {};
    const chest = d.chest || [];
    const back = d.back || [];
    const shoulders = d.shoulders || [];
    const arms = d.arms || [];
    const core = d.core || [];
    const legs = d.legs || [];
    const cardio = d.cardio || [];

    if (topFilter === "upper") {
      return [
        { key: "Chest", items: chest },
        { key: "Back", items: back },
        { key: "Shoulders", items: shoulders },
        { key: "Arms", items: arms },
      ];
    }

    if (topFilter === "lower") {
      return [
        { key: "Core", items: core },
        { key: "Legs", items: legs },
        { key: "Cardio", items: cardio },
      ];
    }

    return [
      { key: "Chest", items: chest },
      { key: "Back", items: back },
      { key: "Shoulders", items: shoulders },
      { key: "Arms", items: arms },
      { key: "Core", items: core },
      { key: "Legs", items: legs },
      { key: "Cardio", items: cardio },
    ];
  }, [data, topFilter]);

  const allTiles = useMemo(() => {
    const d = data || {};
    return []
      .concat(
        d.chest || [],
        d.back || [],
        d.shoulders || [],
        d.arms || [],
        d.core || [],
        d.legs || [],
        d.cardio || []
      )
      .filter((x) => x && x.id && x.name);
  }, [data]);

  const searched = useMemo(() => {
    const query = q.trim().toLowerCase();
    if (!query) return allTiles.slice(0, 60);

    return allTiles.filter((x) => {
      const name = String(x.name || "").toLowerCase();
      const cat = String(x.category || "").toLowerCase();
      return name.includes(query) || cat.includes(query);
    }).slice(0, 80);
  }, [q, allTiles]);

  async function openDetail(tile) {
    setActive(tile);
    setDetail(null);
    setDetailBusy(true);

    try {
      const res = await apiClient.get(`/api/exercises/${tile.id}`);
      setDetail(res.data);
    } catch {
      setDetail({ name: tile.name, description: "", images: [], muscles: [], equipment: [] });
    } finally {
      setDetailBusy(false);
    }
  }

  const modalImages = useMemo(() => {
    const imgs = (detail?.images || []).filter(Boolean);
    if (imgs.length > 0) return imgs.slice(0, 4);
    if (active) return [bestImage(active)];
    return [];
  }, [detail, active]);

  return (
    <div className="pageShell">
      <div className="eeTop">
        <button className="eeBackBtn" type="button" onClick={() => navigate("/fitness")}>
          Back
        </button>

        <div className="eeTitle">Exercise Encyclopedia</div>

        <button className="eeSearchBtn" type="button" onClick={() => setSearchOpen(true)}>
          🔍
        </button>
      </div>

      <div className="pageBody eeBody">
        <div className="eeTabs">
          <button
            type="button"
            className={topFilter === "all" ? "eeTab eeTabOn" : "eeTab"}
            onClick={() => setTopFilter("all")}
          >
            All
          </button>
          <button
            type="button"
            className={topFilter === "upper" ? "eeTab eeTabOn" : "eeTab"}
            onClick={() => setTopFilter("upper")}
          >
            Upper Body
          </button>
          <button
            type="button"
            className={topFilter === "lower" ? "eeTab eeTabOn" : "eeTab"}
            onClick={() => setTopFilter("lower")}
          >
            Lower Body
          </button>
        </div>

        {loading && <div className="eeHint">Loading exercises…</div>}
        {err && <div className="eeErr">{err}</div>}

        {groups.map((g) => (
          <Row
            key={g.key}
            title={g.key}
            items={g.items}
            onPick={openDetail}
            onSeeAll={() => {
              setSearchOpen(true);
              setQ(g.key);
            }}
          />
        ))}
      </div>

      {searchOpen && (
        <div className="eeSearchOverlay">
          <div className="eeSearchPanel">
            <div className="eeSearchTop">
              <button className="eeCloseBtn" type="button" onClick={() => setSearchOpen(false)}>
                Back
              </button>
              <div className="eeSearchTitle">Search</div>
              <div />
            </div>

            <div className="eeSearchBox">
              <span>🔍</span>
              <input
                className="eeSearchInput"
                value={q}
                onChange={(e) => setQ(e.target.value)}
                placeholder="Search exercises"
                autoFocus
              />
              <button className="eeClear" onClick={() => setQ("")}>
                ✕
              </button>
            </div>

            <div className="eeSearchResults">
              {searched.map((x) => (
                <button
                  key={x.id}
                  type="button"
                  className="eeSearchRow"
                  onClick={() => {
                    setSearchOpen(false);
                    openDetail(x);
                  }}
                >
                  <div className="eeMiniImg">
                    <img className="eeMiniImgEl" src={bestImage(x)} alt="" />
                  </div>
                  <div>
                    <div className="eeMiniName">{x.name}</div>
                    <div className="eeMiniMeta">{x.category}</div>
                  </div>
                </button>
              ))}
            </div>
          </div>
        </div>
      )}

      {active && (
        <div className="eeModal">
          <button className="eeModalBg" onClick={() => setActive(null)} />
          <div className="eeModalCard">
            <div className="eeModalHead">
              <div className="eeModalTitle">{detail?.name || active.name}</div>
              <button className="eeX" onClick={() => setActive(null)}>
                ✕
              </button>
            </div>

            {!detailBusy && (
              <div className="eeModalBody">
                <div className="eeModalImgs">
                  {modalImages.map((u) => (
                    <div className="eeModalImgBox" key={u}>
                      <img className="eeModalImg" src={u} alt="" />
                    </div>
                  ))}
                </div>

                <div className="eeBlock">
                  <div className="eeBlockTitle">Description</div>
                  <div className="eeBlockText">
                    {cleanHtmlToText(detail?.description) || "No description available."}
                  </div>
                </div>
              </div>
            )}
          </div>
        </div>
      )}
    </div>
  );
}
