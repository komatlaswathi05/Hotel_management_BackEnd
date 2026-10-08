// Shared helpers for the login / signup / forgot-password pages.
(function () {
  // Calls the Customer API. Never throws: always resolves to
  // { ok, status, data, network, parsed } so pages can show a clear message.
  async function post(path, payload) {
    const url = `${API_BASE_URL}${path}`;
    const ctrl = new AbortController();
    const timer = setTimeout(() => ctrl.abort(), 15000);
    try {
      const res = await fetch(url, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload),
        signal: ctrl.signal,
      });
      const text = await res.text();
      let data = null;
      try { data = text ? JSON.parse(text) : null; } catch (e) { data = text; }
      return { ok: res.ok, status: res.status, data, url };
    } catch (e) {
      return { ok: false, status: 0, network: true, timeout: e.name === "AbortError", url };
    } finally {
      clearTimeout(timer);
    }
  }

  // Turns a failed response into { general, fields } for display.
  function explain(r) {
    if (r.network) {
      return {
        general: r.timeout
          ? "The server took too long to respond. Please try again."
          : `Cannot reach the Customer Service at ${r.url.replace(/\/api\/.*/, "")}. Make sure it is running.`,
        fields: {},
      };
    }
    const d = r.data;
    if (d && typeof d === "object") {
      if (d.message) return { general: d.message, fields: {} };
      // Validation errors come back as { fieldName: "message" }
      const fields = {};
      Object.keys(d).forEach((k) => { if (typeof d[k] === "string") fields[k] = d[k]; });
      if (Object.keys(fields).length) return { general: "", fields };
    }
    return { general: `Request failed (HTTP ${r.status}). Please try again.`, fields: {} };
  }

  function setBusy(btn, busy, label) {
    btn.disabled = busy;
    btn.innerHTML = busy ? `<span class="spin"></span> ${label}` : label;
  }

  function showAlert(box, type, text) {
    box.className = "alert " + type;
    box.textContent = text;
    box.hidden = !text;
  }

  function setFieldError(input, text) {
    const field = input.closest(".field");
    field.classList.toggle("invalid", !!text);
    let el = field.querySelector(".err");
    if (!el) { el = document.createElement("div"); el.className = "err"; field.appendChild(el); }
    el.textContent = text || "";
  }

  function clearFieldErrors(form) {
    form.querySelectorAll(".field").forEach((f) => {
      f.classList.remove("invalid");
      const e = f.querySelector(".err");
      if (e) e.textContent = "";
    });
  }

  function wireToggles() {
    document.querySelectorAll(".toggle").forEach((btn) => {
      btn.addEventListener("click", () => {
        const input = btn.parentElement.querySelector("input");
        const show = input.type === "password";
        input.type = show ? "text" : "password";
        btn.textContent = show ? "Hide" : "Show";
      });
    });
  }

  function loggedIn() {
    try { return JSON.parse(localStorage.getItem(SESSION_KEY) || "null"); } catch (e) { return null; }
  }

  window.Auth = { post, explain, setBusy, showAlert, setFieldError, clearFieldErrors, wireToggles, loggedIn };
})();
