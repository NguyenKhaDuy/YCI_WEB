(function () {
    const CART_KEY_PREFIX = "yci.rental.cart";
    const LEGACY_CART_KEY = CART_KEY_PREFIX;
    const FALLBACK_IMAGE = "/images/logo.png";
    let cartIds = [];
    let cartLoaded = false;

    function qs(selector, root = document) {
        return root.querySelector(selector);
    }

    function qsa(selector, root = document) {
        return Array.from(root.querySelectorAll(selector));
    }

    function cleanText(value) {
        let text = String(value ?? "");
        if (!/[ÃÂÄÆáºá»]/.test(text) || typeof TextDecoder === "undefined") {
            return text;
        }
        for (let index = 0; index < 2; index += 1) {
            try {
                const bytes = Uint8Array.from(Array.from(text, (char) => char.charCodeAt(0) & 255));
                const decoded = new TextDecoder("utf-8", { fatal: true }).decode(bytes);
                if (!decoded || decoded === text) {
                    break;
                }
                text = decoded;
            } catch (error) {
                break;
            }
        }
        return text;
    }

    function escapeHtml(value) {
        return cleanText(value)
            .replaceAll("&", "&amp;")
            .replaceAll("<", "&lt;")
            .replaceAll(">", "&gt;")
            .replaceAll('"', "&quot;")
            .replaceAll("'", "&#039;");
    }

    async function request(url, options = {}) {
        const response = await fetch(url, options);
        const text = await response.text();
        let body = {};
        if (text) {
            try {
                body = JSON.parse(text);
            } catch (error) {
                body = { message: text };
            }
        }
        if (!response.ok) {
            throw new Error(body.message || "Yêu cầu không thành công");
        }
        if (body.status && ["BAD_REQUEST", "NOT_FOUND", "UNAUTHORIZED", "BAD_GATEWAY"].includes(body.status)) {
            throw new Error(body.message || "Yêu cầu không thành công");
        }
        return body;
    }

    function apiGet(url) {
        return request(url);
    }

    function apiJson(url, method, payload) {
        return request(url, {
            method,
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(payload)
        });
    }

    function apiForm(url, method, formData) {
        return request(url, { method, body: formData });
    }

    function unwrapList(response) {
        if (!response) {
            return [];
        }
        if (Array.isArray(response)) {
            return response;
        }
        if (Array.isArray(response.data)) {
            return response.data;
        }
        return [];
    }

    function unwrapData(response) {
        return response && Object.prototype.hasOwnProperty.call(response, "data") ? response.data : response;
    }

    function money(value) {
        const number = Number(value || 0);
        return new Intl.NumberFormat("vi-VN", {
            style: "currency",
            currency: "VND",
            maximumFractionDigits: 0
        }).format(number);
    }

    function formatDate(value) {
        if (!value) {
            return "-";
        }
        const date = new Date(value);
        if (Number.isNaN(date.getTime())) {
            return String(value).replace("T", " ");
        }
        return new Intl.DateTimeFormat("vi-VN", {
            dateStyle: "short",
            timeStyle: "short"
        }).format(date);
    }

    function toBackendDate(localDateTimeValue) {
        if (!localDateTimeValue) {
            return "";
        }
        const [datePart, timePart = "00:00"] = localDateTimeValue.split("T");
        const [year, month, day] = datePart.split("-");
        return `${day}-${month}-${year} ${timePart}:00`;
    }

    function formatLocalDateTimeInput(date) {
        const pad = (value) => String(value).padStart(2, "0");
        return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}T${pad(date.getHours())}:${pad(date.getMinutes())}`;
    }

    function addHoursToLocalValue(localDateTimeValue, hours) {
        const date = new Date(localDateTimeValue);
        if (!localDateTimeValue || Number.isNaN(date.getTime()) || !hours) {
            return "";
        }
        date.setMinutes(date.getMinutes() + Math.round(Number(hours) * 60));
        return formatLocalDateTimeInput(date);
    }

    function rentalDurationHours(type) {
        const normalized = String(type || "").trim().toUpperCase().replace(",", ".");
        const hourMatch = normalized.match(/(\d+(?:\.\d+)?)\s*(H|GIỜ|GIO|HOUR)/);
        if (hourMatch) {
            return Number(hourMatch[1]);
        }
        const dayMatch = normalized.match(/(\d+(?:\.\d+)?)\s*(D|DAY|NGÀY|NGAY)/);
        if (dayMatch) {
            return Number(dayMatch[1]) * 24;
        }
        return 0;
    }

    function formatDuration(hours) {
        const value = Number(hours || 0);
        if (!value) {
            return "chưa rõ thời lượng";
        }
        if (value % 24 === 0) {
            return `${value / 24} ngày`;
        }
        return `${value} giờ`;
    }

    function productImage(product) {
        const firstImage = product?.imageDTOS?.[0];
        if (product?.idProduct && firstImage?.idImage) {
            return `/api/product/${product.idProduct}/image/${firstImage.idImage}`;
        }
        const raw = firstImage?.imageBase64 || product?.imageBase64;
        if (!raw) {
            return FALLBACK_IMAGE;
        }
        if (raw.startsWith("data:") || raw.startsWith("/")) {
            return raw;
        }
        return `data:image/jpeg;base64,${raw}`;
    }

    function allProductImages(product) {
        const images = product?.imageDTOS || [];
        if (!images.length) {
            return [FALLBACK_IMAGE];
        }
        return images.map((image) => {
            if (product?.idProduct && image.idImage) {
                return `/api/product/${product.idProduct}/image/${image.idImage}`;
            }
            const raw = image.imageBase64;
            return raw?.startsWith("data:") ? raw : `data:image/jpeg;base64,${raw}`;
        });
    }

    function imageFallbackAttribute() {
        return `onerror="this.onerror=null;this.closest('.product-card-media')?.classList.add('is-placeholder');this.src='${FALLBACK_IMAGE}'"`;
    }

    function minPrice(product) {
        const prices = product?.rentalPriceDTOS || [];
        if (!prices.length) {
            return 0;
        }
        return Math.min(...prices.map((price) => Number(price.price || 0)));
    }

    function hasProductImage(product) {
        return Boolean((product?.imageDTOS || []).length || product?.imageBase64);
    }

    function isProductAvailable(product) {
        return String(product?.status || "").toUpperCase().includes("AVAILABLE");
    }

    function statusClass(status) {
        const normalized = String(status || "").toUpperCase();
        if (normalized.includes("AVAILABLE") || normalized.includes("ACCEPTED") || normalized.includes("SUCCESS")) {
            return "good";
        }
        if (normalized.includes("WAIT") || normalized.includes("PENDING") || normalized.includes("NOT YET")) {
            return "warn";
        }
        if (normalized.includes("RENTED") || normalized.includes("CANCEL") || normalized.includes("MAINTENANCE") || normalized.includes("UNAVAILABLE")) {
            return "bad";
        }
        return "";
    }

    function statusLabel(status) {
        const normalized = String(status || "").trim().toUpperCase();
        const labels = {
            "AVAILABLE": "Sẵn sàng",
            "RENTED": "Đang cho thuê",
            "RENTING": "Đang thuê",
            "MAINTENANCE": "Bảo trì",
            "UNAVAILABLE": "Không khả dụng",
            "WAITING ACCEPT": "Chờ xác nhận",
            "WAITING_ACCEPT": "Chờ xác nhận",
            "ACCEPTED": "Đã xác nhận",
            "CANCELLED": "Đã hủy",
            "COMPLETED": "Hoàn tất",
            "RETURNED": "Đã trả",
            "NOT YET RECEIVED": "Chưa nhận",
            "RECEIVED": "Đã nhận"
        };
        return labels[normalized] || status || "Chưa rõ";
    }

    function roleLabel(role) {
        const value = Number(role);
        if (value === 0) {
            return "Quản trị viên";
        }
        if (value === 2) {
            return "Nhân viên";
        }
        return "Khách hàng";
    }

    function currentUserId() {
        return window.YCI_SESSION?.user?.idUser || null;
    }

    function isAdminUser() {
        return Number(window.YCI_SESSION?.user?.role) === 0;
    }

    function currentCartKey() {
        const userId = currentUserId();
        return userId ? `${CART_KEY_PREFIX}.user.${userId}` : `${CART_KEY_PREFIX}.guest`;
    }

    function clearLegacyCart() {
        try {
            sessionStorage.removeItem(LEGACY_CART_KEY);
            localStorage.removeItem(LEGACY_CART_KEY);
        } catch (error) {
            // Storage can be blocked in private browsing; keep the in-memory cart usable.
        }
    }

    function readStoredCart() {
        try {
            const raw = localStorage.getItem(currentCartKey());
            const parsed = raw ? JSON.parse(raw) : [];
            return Array.isArray(parsed) ? parsed.map((id) => Number(id)).filter(Boolean) : [];
        } catch (error) {
            return [];
        }
    }

    function storeCart(ids) {
        try {
            localStorage.setItem(currentCartKey(), JSON.stringify(ids));
        } catch (error) {
            toast("Trình duyệt đang chặn lưu giỏ hàng. Giỏ hiện chỉ giữ trong phiên mở trang.", "error");
        }
    }

    async function loadCart(force = false) {
        clearLegacyCart();
        if (isAdminUser()) {
            cartIds = [];
            cartLoaded = true;
            updateCartCount();
            return cartIds;
        }
        if (cartLoaded && !force) {
            return cartIds;
        }
        cartIds = readStoredCart();
        cartLoaded = true;
        updateCartCount();
        return cartIds;
    }

    async function ensureCartLoaded() {
        if (!cartLoaded) {
            await loadCart();
        }
        return cartIds;
    }

    function readCart() {
        return cartIds;
    }

    async function writeCart(ids) {
        const userId = currentUserId();
        if (isAdminUser()) {
            cartIds = [];
            updateCartCount();
            toast("Tài khoản quản trị chỉ được xem, không thể thêm hoặc đặt thuê.", "error");
            return cartIds;
        }
        if (!userId) {
            cartIds = [];
            updateCartCount();
            toast("Vui lòng đăng nhập để tiếp tục đặt thuê.", "error");
            window.setTimeout(() => window.location.assign("/login"), 600);
            return cartIds;
        }
        const uniqueIds = Array.from(new Set(ids.map((id) => Number(id)).filter(Boolean)));
        cartIds = uniqueIds;
        storeCart(uniqueIds);
        updateCartCount();
        return cartIds;
    }

    async function addToCart(id) {
        const userId = currentUserId();
        if (isAdminUser()) {
            toast("Tài khoản quản trị chỉ được xem, không thể thêm hoặc đặt thuê.", "error");
            return [];
        }
        if (!userId) {
            toast("Vui lòng đăng nhập để thêm thiết bị vào đơn thuê.", "error");
            window.setTimeout(() => window.location.assign("/login"), 600);
            return [];
        }
        await ensureCartLoaded();
        const nextIds = Array.from(new Set([...cartIds, Number(id)].filter(Boolean)));
        await writeCart(nextIds);
        toast("Đã thêm thiết bị vào đơn thuê.", "success");
        return cartIds;
    }

    async function removeFromCart(id) {
        await ensureCartLoaded();
        return writeCart(cartIds.filter((itemId) => Number(itemId) !== Number(id)));
    }

    async function clearCart() {
        return writeCart([]);
    }

    function updateCartCount() {
        const count = readCart().length;
        qsa("[data-cart-count], [data-cart-count-inline]").forEach((node) => {
            node.textContent = String(count);
        });
    }

    function productCard(product) {
        const prices = product.rentalPriceDTOS || [];
        const hasImage = hasProductImage(product);
        const available = isProductAvailable(product);
        const lowestPrice = minPrice(product);
        const priceHtml = prices.length
            ? prices.slice(0, 3).map((price) => `<span class="price-chip">${escapeHtml(price.rentalType)} ${money(price.price)}</span>`).join("")
            : `<span class="price-chip subtle">Chưa có giá</span>`;
        const orderAction = (() => {
            if (isAdminUser()) {
                return `<span class="price-chip subtle">Chỉ xem</span>`;
            }
            if (!available) {
                return `<span class="price-chip subtle">Tạm chưa thuê</span>`;
            }
            return `<button class="button primary compact" type="button" data-add-product="${product.idProduct}"><i data-lucide="plus"></i> Thêm</button>`;
        })();
        return `
            <article class="product-card">
                <a class="product-card-media ${hasImage ? "" : "is-placeholder"}" href="/products/${product.idProduct}">
                    <img src="${productImage(product)}" alt="${escapeHtml(product.nameProduct)}" ${imageFallbackAttribute()}>
                    ${!hasImage ? `<span class="image-placeholder-label">YCI Camera</span>` : ""}
                </a>
                <div class="product-card-body">
                    <div class="card-meta">
                        <span class="status-pill ${statusClass(product.status)}">${escapeHtml(statusLabel(product.status))}</span>
                        <span class="category-label">${escapeHtml(product.categoryName || "Thiết bị")}</span>
                    </div>
                    <h3><a href="/products/${product.idProduct}">${escapeHtml(product.nameProduct)}</a></h3>
                    <p class="muted">${escapeHtml(product.brand || "YCI Camera")}</p>
                    <div class="product-card-price">
                        <small>Giá từ</small>
                        <strong>${lowestPrice ? money(lowestPrice) : "Liên hệ"}</strong>
                    </div>
                    <div class="price-list">${priceHtml}</div>
                    <div class="deposit-line">
                        <span>Cọc tham khảo</span>
                        <strong>${money(product.depositPrice)}</strong>
                    </div>
                    <div class="product-actions">
                        <a class="button ghost" href="/products/${product.idProduct}">Chi tiết</a>
                        ${orderAction}
                    </div>
                </div>
            </article>
        `;
    }

    function bookingSummary(booking) {
        const paid = (booking.paymentBookingDTOS || []).reduce((sum, payment) => sum + Number(payment.price || 0), 0);
        const remain = (booking.paymentBookingDTOS || []).reduce((sum, payment) => sum + Number(payment.remainAmount || 0), 0);
        return { paid, remain };
    }

    function setOptions(select, items, getValue, getLabel, placeholder) {
        if (!select) {
            return;
        }
        select.innerHTML = placeholder ? `<option value="">${escapeHtml(placeholder)}</option>` : "";
        items.forEach((item) => {
            const option = document.createElement("option");
            option.value = getValue(item);
            option.textContent = cleanText(getLabel(item));
            select.appendChild(option);
        });
    }

    function formObject(form) {
        return Object.fromEntries(new FormData(form).entries());
    }

    function toast(message, type = "info") {
        let stack = qs("[data-toast-stack]");
        if (!stack) {
            stack = document.createElement("section");
            stack.className = "flash-stack";
            stack.dataset.toastStack = "true";
            document.body.appendChild(stack);
        }
        const node = document.createElement("div");
        node.className = `flash ${type === "error" ? "error" : type === "success" ? "success" : ""}`;
        node.textContent = message;
        stack.appendChild(node);
        window.setTimeout(() => node.remove(), 4200);
    }

    function iconRefresh() {
        if (window.lucide) {
            window.lucide.createIcons();
        }
    }

    document.addEventListener("click", (event) => {
        const addButton = event.target.closest("[data-add-product]");
        if (addButton) {
            addToCart(addButton.dataset.addProduct);
            iconRefresh();
        }
    });

    document.addEventListener("DOMContentLoaded", () => {
        loadCart();
        updateCartCount();
        iconRefresh();
    });

    window.YCI = {
        qs,
        qsa,
        request,
        apiGet,
        apiJson,
        apiForm,
        unwrapList,
        unwrapData,
        cleanText,
        escapeHtml,
        money,
        formatDate,
        toBackendDate,
        addHoursToLocalValue,
        rentalDurationHours,
        formatDuration,
        productImage,
        allProductImages,
        minPrice,
        hasProductImage,
        isProductAvailable,
        statusClass,
        statusLabel,
        roleLabel,
        isAdminUser,
        loadCart,
        readCart,
        writeCart,
        addToCart,
        removeFromCart,
        clearCart,
        updateCartCount,
        productCard,
        imageFallbackAttribute,
        bookingSummary,
        setOptions,
        formObject,
        toast,
        iconRefresh,
        FALLBACK_IMAGE
    };
})();
