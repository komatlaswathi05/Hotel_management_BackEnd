(function () {
  // ---------- Session guard ----------
  const user = JSON.parse(localStorage.getItem(SESSION_KEY) || "null");
  if (!user || !user.customerId) {
    window.location.replace("login.html");
    return;
  }

  // ---------- Room "catalogue" (images + copy are chosen from the room type) ----------
  const IMG = (id) => `https://images.unsplash.com/${id}?auto=format&fit=crop&w=900&q=75`;
  const TIERS = {
    standard: {
      label: "Standard",
      images: ["photo-1590490360182-c33d57733427", "photo-1631049307264-da0ec9d70304", "photo-1618773928121-c32242e63f39", "photo-1611892440504-42a792e24d32"],
      desc: "A cosy, well-kept room with everything you need for a comfortable night's sleep.",
      amenities: ["Free Wi-Fi", "Air conditioning", "Flat-screen TV", "Daily housekeeping"],
    },
    deluxe: {
      label: "Deluxe",
      images: ["photo-1582719478250-c89cae4dc85b", "photo-1566073771259-6a8506099945", "photo-1631049307264-da0ec9d70304", "photo-1590490360182-c33d57733427"],
      desc: "A spacious room with premium bedding, a work desk and a relaxing seating area.",
      amenities: ["Free Wi-Fi", "King bed", "Mini bar", "Breakfast", "Work desk"],
    },
    family: {
      label: "Family",
      images: ["photo-1611892440504-42a792e24d32", "photo-1618773928121-c32242e63f39", "photo-1551882547-ff40c63fe5fa", "photo-1590490360182-c33d57733427"],
      desc: "Extra space for everyone, with flexible bedding so the whole family can rest easily.",
      amenities: ["Free Wi-Fi", "Extra beds", "Breakfast", "Kids friendly", "Air conditioning"],
    },
    suite: {
      label: "Suite",
      images: ["photo-1578683010236-d716f9a3f461", "photo-1551882547-ff40c63fe5fa", "photo-1520250497591-112f2f40a3f4", "photo-1582719478250-c89cae4dc85b"],
      desc: "Our finest accommodation: a separate living area, premium comforts and priority service.",
      amenities: ["Free Wi-Fi", "Living area", "Jacuzzi", "Breakfast", "Priority service", "Mini bar"],
    },
  };

  function tierOf(room) {
    const t = String(room.roomType || "").toLowerCase();
    if (/suite|presidential|royal|luxury|premium|villa/.test(t)) return "suite";
    if (/family|triple|quad/.test(t)) return "family";
    if (/deluxe|executive|superior/.test(t)) return "deluxe";
    if (/standard|single|double|economy|basic|twin/.test(t)) return "standard";
    if (room.price >= 5000) return "suite";
    if (room.price >= 3000) return "deluxe";
    return "standard";
  }

  function imagesFor(room) {
    const list = TIERS[tierOf(room)].images;
    const start = Math.abs(room.roomNumber) % list.length;
    return list.map((_, i) => IMG(list[(start + i) % list.length]));
  }

  // ---------- Helpers ----------
  const $ = (id) => document.getElementById(id);
  const esc = (s) => String(s ?? "").replace(/[&<>"']/g, (c) => ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;" }[c]));
  const money = (n) => "₹" + Number(n).toLocaleString("en-IN", { maximumFractionDigits: 2 });
  const pad = (n) => String(n).padStart(2, "0");
  const toISO = (d) => `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`;
  const parseISO = (s) => { const [y, m, d] = s.split("-").map(Number); return Date.UTC(y, m - 1, d); };
  const nightsBetween = (a, b) => Math.round((parseISO(b) - parseISO(a)) / 86400000);
  const fmtDate = (s) => new Date(parseISO(s)).toLocaleDateString("en-GB", { day: "2-digit", month: "short", year: "numeric", timeZone: "UTC" });

  function toast(msg, type) {
    const el = document.createElement("div");
    el.className = "toast " + (type || "ok");
    el.textContent = msg;
    $("toasts").appendChild(el);
    setTimeout(() => el.remove(), 4000);
  }

  // Calls an API and always returns { ok, data } - data is parsed JSON, or text if not JSON.
  async function api(url, options) {
    try {
      const res = await fetch(url, options);
      const text = await res.text();
      let data = text;
      try { data = JSON.parse(text); } catch (e) { /* plain text response */ }
      return { ok: res.ok, data };
    } catch (e) {
      return { ok: false, data: null, network: true };
    }
  }

  function errorText(r, service) {
    if (r.network) return `Cannot reach ${service}. Make sure it is running (and Eureka for Room/Booking/Billing).`;
    if (r.data && typeof r.data === "object") {
      if (r.data.message) return r.data.message;
      const vals = Object.values(r.data);
      if (vals.length) return vals.join(", ");
    }
    return typeof r.data === "string" && r.data ? r.data : "Something went wrong. Please try again.";
  }

  // ---------- State ----------
  const state = { rooms: [], type: "All", maxPrice: 0, sort: "low", checkIn: "", checkOut: "", selected: null, roomMap: null };

  // ---------- Header ----------
  const displayName = user.username || user.email || "Guest";
  $("userName").textContent = displayName;
  $("avatar").textContent = displayName.charAt(0).toUpperCase();
  $("heroTitle").textContent = `Welcome, ${displayName}`;
  $("logoutBtn").addEventListener("click", () => {
    localStorage.removeItem(SESSION_KEY);
    window.location.href = "login.html";
  });

  // ---------- Tabs ----------
  function showTab(name) {
    const bookings = name === "bookings";
    $("roomsView").hidden = bookings;
    $("bookingsView").hidden = !bookings;
    $("tabRooms").classList.toggle("active", !bookings);
    $("tabBookings").classList.toggle("active", bookings);
    if (bookings) loadBookings();
  }
  $("tabRooms").addEventListener("click", () => { history.replaceState(null, "", "rooms.html"); showTab("rooms"); });
  $("tabBookings").addEventListener("click", () => { history.replaceState(null, "", "#bookings"); showTab("bookings"); });

  // ---------- Dates ----------
  const today = new Date();
  const tomorrow = new Date(today.getTime() + 86400000);
  $("checkIn").min = toISO(today);
  $("checkIn").value = toISO(today);
  $("checkOut").min = toISO(tomorrow);
  $("checkOut").value = toISO(tomorrow);

  function updateNights() {
    const a = $("checkIn").value, b = $("checkOut").value;
    const n = a && b ? nightsBetween(a, b) : 0;
    $("nightsPill").innerHTML = n > 0 ? `<b>${n}</b> night${n > 1 ? "s" : ""}` : "Choose valid dates";
    return n;
  }
  $("checkIn").addEventListener("change", () => {
    const next = new Date(parseISO($("checkIn").value) + 86400000);
    $("checkOut").min = next.toISOString().slice(0, 10);
    if ($("checkOut").value <= $("checkIn").value) $("checkOut").value = next.toISOString().slice(0, 10);
    updateNights();
  });
  $("checkOut").addEventListener("change", updateNights);
  updateNights();

  // ---------- Search availability ----------
  $("searchForm").addEventListener("submit", (e) => { e.preventDefault(); searchRooms(); });

  async function searchRooms() {
    const checkIn = $("checkIn").value, checkOut = $("checkOut").value;
    if (nightsBetween(checkIn, checkOut) < 1) {
      toast("Check-out must be after check-in.", "err");
      return;
    }
    state.checkIn = checkIn;
    state.checkOut = checkOut;
    $("roomGrid").innerHTML = '<div class="skeleton"></div>'.repeat(3);
    $("resultCount").textContent = "Checking availability...";

    const r = await api(`${ROOM_API_URL}/availablerooms?checkIn=${checkIn}&checkOut=${checkOut}`);
    if (!r.ok) {
      $("roomGrid").innerHTML = "";
      $("resultCount").textContent = "";
      $("typeChips").innerHTML = "";
      $("roomGrid").innerHTML = emptyBox("⚠️", "Could not load rooms", esc(errorText(r, "Room Service (port 8083)")));
      return;
    }
    state.rooms = Array.isArray(r.data) ? r.data : [];
    state.type = "All";
    const prices = state.rooms.map((x) => x.price);
    const range = $("priceRange");
    if (prices.length) {
      range.min = Math.floor(Math.min(...prices));
      range.max = Math.ceil(Math.max(...prices));
      range.value = range.max;
      state.maxPrice = Number(range.max);
    }
    range.disabled = prices.length === 0;
    render();
  }

  function emptyBox(icon, title, text) {
    return `<div class="empty" style="grid-column:1/-1"><div class="big">${icon}</div><h3>${title}</h3><p>${text}</p></div>`;
  }

  // ---------- Filters ----------
  $("priceRange").addEventListener("input", (e) => { state.maxPrice = Number(e.target.value); render(); });
  $("sortBy").addEventListener("change", (e) => { state.sort = e.target.value; render(); });

  function renderChips() {
    const types = ["All", ...new Set(state.rooms.map((r) => r.roomType))];
    $("typeChips").innerHTML = types
      .map((t) => `<button type="button" class="chip ${t === state.type ? "active" : ""}" data-type="${esc(t)}">${esc(t)}</button>`)
      .join("");
    $("typeChips").querySelectorAll(".chip").forEach((c) =>
      c.addEventListener("click", () => { state.type = c.dataset.type; render(); })
    );
  }

  function render() {
    renderChips();
    $("priceLabel").textContent = state.rooms.length ? money(state.maxPrice) : "";
    const nights = nightsBetween(state.checkIn, state.checkOut);

    let list = state.rooms.filter((r) => (state.type === "All" || r.roomType === state.type) && r.price <= state.maxPrice);
    if (state.sort === "low") list.sort((a, b) => a.price - b.price);
    else if (state.sort === "high") list.sort((a, b) => b.price - a.price);
    else list.sort((a, b) => a.roomNumber - b.roomNumber);

    $("resultCount").textContent = state.rooms.length
      ? `${list.length} of ${state.rooms.length} available room${state.rooms.length > 1 ? "s" : ""} · ${fmtDate(state.checkIn)} → ${fmtDate(state.checkOut)}`
      : "";

    if (!state.rooms.length) {
      $("roomGrid").innerHTML = emptyBox("🛏️", "No rooms available", "Every room is taken for these dates. Try different dates.");
      return;
    }
    if (!list.length) {
      $("roomGrid").innerHTML = emptyBox("🔎", "No rooms match your filters", "Try another room type or raise the maximum price.");
      return;
    }

    $("roomGrid").innerHTML = list.map((room) => {
      const tier = TIERS[tierOf(room)];
      return `
      <article class="room-card">
        <div class="room-img">
          <img src="${imagesFor(room)[0]}" alt="${esc(room.roomType)}" loading="lazy" />
          <span class="badge">Available</span>
          <span class="badge tier">${tier.label}</span>
        </div>
        <div class="room-body">
          <div><h3>${esc(room.roomType)}</h3><div class="room-no">Room #${room.roomNumber}</div></div>
          <p class="room-desc">${esc(tier.desc)}</p>
          <div class="amenities">${tier.amenities.slice(0, 4).map((a) => `<span class="amenity">${esc(a)}</span>`).join("")}</div>
          <div class="room-foot">
            <div>
              <div class="price">${money(room.price)} <small>/ night</small></div>
              <div class="total-line">${nights} night${nights > 1 ? "s" : ""}: <b>${money(room.price * nights)}</b></div>
            </div>
            <div class="card-actions">
              <button class="btn-dark btn-sm" data-details="${room.roomNumber}" type="button">Details</button>
              <button class="btn-gold btn-sm" data-book="${room.roomNumber}" type="button">Book</button>
            </div>
          </div>
        </div>
      </article>`;
    }).join("");

    $("roomGrid").querySelectorAll("[data-details],[data-book]").forEach((btn) =>
      btn.addEventListener("click", () => openModal(Number(btn.dataset.details || btn.dataset.book)))
    );
  }

  // ---------- Modal ----------
  function openModal(roomNumber) {
    const room = state.rooms.find((r) => r.roomNumber === roomNumber);
    if (!room) return;
    state.selected = room;
    const tier = TIERS[tierOf(room)];
    const imgs = imagesFor(room);
    const nights = nightsBetween(state.checkIn, state.checkOut);

    $("galleryMain").src = imgs[0];
    $("thumbs").innerHTML = imgs.map((u, i) => `<img src="${u}" class="${i === 0 ? "on" : ""}" alt="Photo ${i + 1}" />`).join("");
    $("thumbs").querySelectorAll("img").forEach((t) =>
      t.addEventListener("click", () => {
        $("galleryMain").src = t.src;
        $("thumbs").querySelectorAll("img").forEach((x) => x.classList.remove("on"));
        t.classList.add("on");
      })
    );
    $("mRoomNo").textContent = `Room #${room.roomNumber} · ${tier.label}`;
    $("mTitle").textContent = room.roomType;
    $("mDesc").textContent = tier.desc;
    $("mAmenities").innerHTML = tier.amenities.map((a) => `<span class="amenity">${esc(a)}</span>`).join("");
    $("mBreakdown").innerHTML = `
      <div><span>Check-in</span><span>${fmtDate(state.checkIn)}</span></div>
      <div><span>Check-out</span><span>${fmtDate(state.checkOut)}</span></div>
      <div><span>${money(room.price)} × ${nights} night${nights > 1 ? "s" : ""}</span><span>${money(room.price * nights)}</span></div>
      <div class="sum"><span>Total</span><span>${money(room.price * nights)}</span></div>`;
    $("mError").textContent = "";
    $("confirmBtn").disabled = false;
    $("confirmBtn").textContent = "Confirm booking";
    $("modalBack").hidden = false;
    document.body.style.overflow = "hidden";
  }

  function closeModal() {
    $("modalBack").hidden = true;
    document.body.style.overflow = "";
  }
  $("modalClose").addEventListener("click", closeModal);
  $("modalBack").addEventListener("click", (e) => { if (e.target === $("modalBack")) closeModal(); });
  document.addEventListener("keydown", (e) => { if (e.key === "Escape") closeModal(); });

  $("confirmBtn").addEventListener("click", async () => {
    const room = state.selected;
    if (!room) return;
    $("confirmBtn").disabled = true;
    $("confirmBtn").textContent = "Booking...";
    $("mError").textContent = "";

    const r = await api(BOOKING_API_URL, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        customerId: user.customerId,
        roomNumber: room.roomNumber,
        checkInDate: state.checkIn,
        checkOutDate: state.checkOut,
        status: "Confirmed",
      }),
    });

    if (r.ok) {
      closeModal();
      toast(`Booking #${r.data.bookingId} confirmed for room #${room.roomNumber}!`);
      searchRooms();
    } else {
      $("mError").textContent = errorText(r, "Booking Service (port 8082)");
      $("confirmBtn").disabled = false;
      $("confirmBtn").textContent = "Confirm booking";
    }
  });

  // ---------- My bookings ----------
  async function loadBookings() {
    const box = $("bookingList");
    box.innerHTML = '<div class="skeleton" style="height:130px"></div>'.repeat(2);

    if (!state.roomMap) {
      const rooms = await api(ROOM_API_URL);
      if (rooms.ok && Array.isArray(rooms.data)) {
        state.roomMap = {};
        rooms.data.forEach((r) => { state.roomMap[r.roomNumber] = r; });
      }
    }

    const r = await api(`${BOOKING_API_URL}/history?customerId=${user.customerId}`);
    if (!r.ok) {
      box.innerHTML = emptyBox("⚠️", "Could not load bookings", esc(errorText(r, "Booking Service (port 8082)")));
      return;
    }
    const list = (Array.isArray(r.data) ? r.data : []).sort((a, b) => b.bookingId - a.bookingId);
    if (!list.length) {
      box.innerHTML = emptyBox("🧳", "No bookings yet", 'Find a room on the <b>Rooms</b> tab and book your first stay.');
      return;
    }

    box.innerHTML = list.map((b) => {
      const room = (state.roomMap && state.roomMap[b.roomNumber]) || { roomNumber: b.roomNumber, roomType: "Room", price: 0 };
      const nights = nightsBetween(b.checkInDate, b.checkOutDate);
      const st = String(b.status || "");
      const cls = st.toLowerCase() === "confirmed" ? "confirmed" : st.toLowerCase() === "cancelled" ? "cancelled" : "other";
      const total = room.price ? ` · ${money(room.price * nights)}` : "";
      return `
      <div class="booking-item">
        <img src="${imagesFor(room)[0]}" alt="${esc(room.roomType)}" loading="lazy" />
        <div class="booking-info">
          <h3>${esc(room.roomType)} <span class="room-no">· Room #${b.roomNumber}</span></h3>
          <p>${fmtDate(b.checkInDate)} → ${fmtDate(b.checkOutDate)}</p>
          <p>${nights} night${nights > 1 ? "s" : ""}${total}</p>
          <p class="room-no">Booking #${b.bookingId}</p>
        </div>
        <div class="booking-side">
          <span class="status ${cls}">${esc(st)}</span>
          ${cls === "cancelled" ? "" : `<button class="btn-gold btn-danger btn-sm" data-cancel="${b.bookingId}" type="button">Cancel booking</button>`}
        </div>
      </div>`;
    }).join("");

    box.querySelectorAll("[data-cancel]").forEach((btn) =>
      btn.addEventListener("click", async () => {
        if (!confirm("Cancel this booking?")) return;
        btn.disabled = true;
        const res = await api(`${BOOKING_API_URL}/cancel/${btn.dataset.cancel}`, { method: "PUT" });
        if (res.ok) {
          toast("Booking cancelled.");
          loadBookings();
        } else {
          toast(errorText(res, "Booking Service (port 8082)"), "err");
          btn.disabled = false;
        }
      })
    );
  }

  // ---------- Start ----------
  if (location.hash === "#bookings") showTab("bookings");
  searchRooms();
})();
