(function () {
    async function loadProducts() {
        return YCI.unwrapList(await YCI.apiGet("/api/product/all"));
    }

    async function loadCategories() {
        return YCI.unwrapList(await YCI.apiGet("/api/category"));
    }

    function bootHome() {
        Promise.all([loadProducts(), loadCategories()])
            .then(([products, categories]) => {
                const available = products.filter((item) => String(item.status || "").toUpperCase().includes("AVAILABLE"));
                const featured = YCI.qs("[data-featured-products]");
                const strip = YCI.qs("[data-category-strip]");

                YCI.qs("[data-stat-products]").textContent = products.length;
                YCI.qs("[data-stat-categories]").textContent = categories.length;
                YCI.qs("[data-stat-available]").textContent = available.length;

                strip.innerHTML = categories.slice(0, 4).map((category) => `
                    <a class="category-card" href="/products?category=${category.idCategory}">
                        <strong>${YCI.escapeHtml(category.nameCategory)}</strong>
                        <p class="muted">${YCI.escapeHtml(category.description || "Thiết bị phù hợp cho nhiều lịch chụp.")}</p>
                    </a>
                `).join("");

                const featuredProducts = [...products].sort((left, right) => {
                    return Number((right.imageDTOS || []).length > 0) - Number((left.imageDTOS || []).length > 0);
                });
                featured.innerHTML = featuredProducts.slice(0, 6).map(YCI.productCard).join("");
                YCI.iconRefresh();
            })
            .catch((error) => YCI.toast(error.message, "error"));
    }

    function bootCatalog() {
        const state = {
            products: [],
            categories: [],
            search: new URLSearchParams(window.location.search).get("q") || "",
            category: new URLSearchParams(window.location.search).get("category") || "",
            status: ""
        };
        const list = YCI.qs("[data-product-list]");
        const empty = YCI.qs("[data-empty-products]");
        const searchInput = YCI.qs("[data-product-search]");
        const statusFilter = YCI.qs("[data-status-filter]");
        const categoryFilter = YCI.qs("[data-category-filter]");

        function renderFilters() {
            const buttons = [{ idCategory: "", nameCategory: "Tất cả" }, ...state.categories];
            categoryFilter.innerHTML = buttons.map((category) => `
                <button class="segment ${String(category.idCategory) === String(state.category) ? "is-active" : ""}"
                        type="button"
                        data-category-id="${category.idCategory}">
                    ${YCI.escapeHtml(category.nameCategory)}
                </button>
            `).join("");
        }

        function render() {
            const keyword = state.search.trim().toLowerCase();
            const filtered = state.products.filter((product) => {
                const text = YCI.cleanText([product.nameProduct, product.brand, product.serialNumber, product.categoryName].join(" ")).toLowerCase();
                const matchSearch = !keyword || text.includes(keyword);
                const matchCategory = !state.category || String(product.categoryId) === String(state.category);
                const matchStatus = !state.status || String(product.status || "").toUpperCase().includes(state.status);
                return matchSearch && matchCategory && matchStatus;
            });
            list.innerHTML = filtered.map(YCI.productCard).join("");
            empty.hidden = filtered.length > 0;
            YCI.qs("[data-catalog-count]").textContent = state.products.length;
            YCI.qs("[data-catalog-available]").textContent = state.products.filter((item) => String(item.status || "").toUpperCase().includes("AVAILABLE")).length;
            YCI.updateCartCount();
            YCI.iconRefresh();
        }

        searchInput.value = state.search;
        searchInput.addEventListener("input", () => {
            state.search = searchInput.value;
            render();
        });
        statusFilter.addEventListener("change", () => {
            state.status = statusFilter.value;
            render();
        });
        categoryFilter.addEventListener("click", (event) => {
            const button = event.target.closest("[data-category-id]");
            if (!button) {
                return;
            }
            state.category = button.dataset.categoryId;
            renderFilters();
            render();
        });

        Promise.all([loadProducts(), loadCategories()])
            .then(([products, categories]) => {
                state.products = products;
                state.categories = categories;
                renderFilters();
                render();
            })
            .catch((error) => YCI.toast(error.message, "error"));
    }

    function bootProductDetail() {
        const root = YCI.qs("[data-product-id]");
        const id = root?.dataset.productId;
        if (!id) {
            return;
        }
        YCI.apiGet(`/api/product/id-product=${id}`)
            .then((response) => {
                const product = YCI.unwrapData(response);
                const images = YCI.allProductImages(product);
                const mainImage = YCI.qs("[data-product-image]");
                mainImage.onerror = () => {
                    mainImage.onerror = null;
                    mainImage.src = YCI.FALLBACK_IMAGE;
                };
                mainImage.src = images[0];
                mainImage.alt = YCI.cleanText(product.nameProduct || "Thiết bị YCI");
                YCI.qs("[data-product-crumb]").textContent = YCI.cleanText(product.nameProduct || "Chi tiết");
                YCI.qs("[data-product-name]").textContent = YCI.cleanText(product.nameProduct || "Thiết bị");
                YCI.qs("[data-product-description]").textContent = YCI.cleanText(product.description || "Chưa có mô tả.");
                YCI.qs("[data-product-brand]").textContent = YCI.cleanText(product.brand || "-");
                YCI.qs("[data-product-category]").textContent = YCI.cleanText(product.categoryName || "-");
                YCI.qs("[data-product-serial]").textContent = YCI.cleanText(product.serialNumber || "-");
                YCI.qs("[data-product-deposit]").textContent = YCI.money(product.depositPrice);
                const status = YCI.qs("[data-product-status]");
                status.textContent = YCI.statusLabel(product.status);
                status.className = `status-pill ${YCI.statusClass(product.status)}`;
                YCI.qs("[data-product-prices]").innerHTML = (product.rentalPriceDTOS || []).length
                    ? `<div class="price-list">${product.rentalPriceDTOS.map((price) => `<span class="price-chip">${YCI.escapeHtml(price.rentalType)} ${YCI.money(price.price)}</span>`).join("")}</div>`
                    : `<p class="muted">Chưa có giá thuê cho thiết bị này.</p>`;
                YCI.qs("[data-product-thumbs]").innerHTML = images.map((image, index) => `
                    <button class="${index === 0 ? "is-active" : ""}" type="button" data-thumb-index="${index}">
                        <img src="${image}" alt="Ảnh ${index + 1}" ${YCI.imageFallbackAttribute()}>
                    </button>
                `).join("");
                YCI.qs("[data-product-thumbs]").addEventListener("click", (event) => {
                    const button = event.target.closest("[data-thumb-index]");
                    if (!button) {
                        return;
                    }
                    mainImage.src = images[Number(button.dataset.thumbIndex)];
                    YCI.qsa("[data-thumb-index]").forEach((item) => item.classList.toggle("is-active", item === button));
                });
                if (YCI.isAdminUser()) {
                    YCI.qs("[data-product-actions]").innerHTML = `<span class="price-chip subtle">Tài khoản quản trị chỉ xem thông tin, không thể thêm hoặc đặt thuê.</span>`;
                } else {
                    YCI.qs("[data-add-current-product]").addEventListener("click", () => YCI.addToCart(product.idProduct));
                }
                YCI.iconRefresh();
            })
            .catch((error) => YCI.toast(error.message, "error"));
    }

    function bootPricing() {
        function depositTitle(type) {
            const normalized = String(type || "").trim().toUpperCase();
            const labels = {
                BOOKING_EARLY: "Đặt sớm",
                BOOKING_NEAR: "Đặt gần ngày",
                HOLIDAY_BOOKING: "Ngày lễ / cao điểm",
                FULL_DEPOSIT: "Cọc toàn phần",
                PARTIAL_DEPOSIT: "Cọc một phần"
            };
            return labels[normalized] || YCI.cleanText(type || "Chính sách cọc");
        }

        function depositDescription(deposit) {
            const percent = Number(deposit.percentDeposit || 0);
            const title = String(deposit.type || "").toUpperCase();
            if (title.includes("EARLY")) {
                return `Giữ lịch sớm với mức cọc ${percent}% để YCI khóa thiết bị cho ngày chụp.`;
            }
            if (title.includes("NEAR")) {
                return `Áp dụng khi lịch thuê sát ngày, mức cọc ${percent}% giúp xác nhận nhanh thiết bị.`;
            }
            if (title.includes("HOLIDAY")) {
                return `Lịch cao điểm hoặc ngày lễ có thể yêu cầu cọc ${percent}% trước khi giữ máy.`;
            }
            return `Mức cọc ${percent}% trên tổng giá thuê, được nhân viên áp dụng khi xác nhận đơn.`;
        }

        Promise.all([loadProducts(), YCI.apiGet("/api/deposit-type")])
            .then(([products, depositResponse]) => {
                YCI.qs("[data-pricing-table]").innerHTML = products.length ? products.map((product) => `
                    <tr>
                        <td data-label="Thiết bị"><strong>${YCI.escapeHtml(product.nameProduct)}</strong><br><span class="muted">${YCI.escapeHtml(product.brand || "YCI Camera")}</span></td>
                        <td data-label="Danh mục">${YCI.escapeHtml(product.categoryName || "-")}</td>
                        <td data-label="Tình trạng"><span class="status-pill ${YCI.statusClass(product.status)}">${YCI.escapeHtml(YCI.statusLabel(product.status))}</span></td>
                        <td data-label="Giá thuê">${(product.rentalPriceDTOS || []).map((price) => `<span class="price-chip">${YCI.escapeHtml(price.rentalType)} ${YCI.money(price.price)}</span>`).join(" ") || `<span class="price-chip subtle">Chưa có giá</span>`}</td>
                        <td data-label="Cọc tham chiếu"><strong>${YCI.money(product.depositPrice)}</strong></td>
                    </tr>
                `).join("") : `<tr><td colspan="5"><div class="empty-state small"><i data-lucide="tag"></i><h3>Chưa có bảng giá</h3><p>Khi quản trị thêm thiết bị và giá thuê, dữ liệu sẽ hiển thị tại đây.</p></div></td></tr>`;
                const deposits = YCI.unwrapList(depositResponse);
                const fallbackDeposits = [
                    { type: "Đặt lịch tiêu chuẩn", percentDeposit: 30 },
                    { type: "Thiết bị giá trị cao", percentDeposit: 50 },
                    { type: "Lịch cao điểm", percentDeposit: 70 }
                ];
                YCI.qs("[data-deposit-types]").innerHTML = (deposits.length ? deposits : fallbackDeposits).map((deposit) => `
                    <article class="info-card">
                        <span class="price-chip subtle">${Number(deposit.percentDeposit || 0)}%</span>
                        <h3>${YCI.escapeHtml(depositTitle(deposit.type))}</h3>
                        <p class="muted">${YCI.escapeHtml(depositDescription(deposit))}</p>
                    </article>
                `).join("");
                YCI.iconRefresh();
            })
            .catch((error) => YCI.toast(error.message, "error"));
    }

    function bootRegulations() {
        const fallbackGroups = [
            {
                key: "rental",
                icon: "camera",
                eyebrow: "Thuê thiết bị",
                title: "Quy định thuê thiết bị",
                items: [
                    "Khách cần đăng nhập, cung cấp thông tin liên hệ và giấy tờ thế chấp hợp lệ khi tạo đơn thuê.",
                    "Mỗi đơn có thể gồm nhiều thiết bị; giá thuê được tính theo gói giờ/ngày đã chọn.",
                    "YCI chỉ giữ thiết bị sau khi nhân viên xác nhận đơn và mức cọc tương ứng."
                ]
            },
            {
                key: "schedule",
                icon: "calendar-check",
                eyebrow: "Giữ lịch",
                title: "Quy định giữ lịch",
                items: [
                    "Lịch đặt được ưu tiên theo thời điểm gửi yêu cầu và tình trạng sẵn sàng của thiết bị.",
                    "Khách nên gửi yêu cầu sớm để nhân viên kiểm tra trùng lịch trước ngày nhận máy.",
                    "Đơn chưa xác nhận có thể bị hủy nếu thiết bị không còn phù hợp với lịch thuê."
                ]
            },
            {
                key: "pickup",
                icon: "clipboard-check",
                eyebrow: "Nhận máy",
                title: "Nhận thiết bị",
                items: [
                    "Khi nhận máy, khách và nhân viên cùng đối chiếu serial, phụ kiện đi kèm và tình trạng ngoại hình.",
                    "Khách thanh toán cọc hoặc khoản cần thu theo xác nhận trước khi mang thiết bị đi.",
                    "YCI khuyến nghị kiểm tra pin, thẻ nhớ, cap và phụ kiện ngay tại quầy."
                ]
            },
            {
                key: "return",
                icon: "rotate-ccw",
                eyebrow: "Hoàn trả",
                title: "Trả thiết bị",
                items: [
                    "Thiết bị cần được trả đúng giờ theo đơn thuê để tránh phí trả trễ.",
                    "Nhân viên kiểm tra lại tình trạng trước khi tất toán hoặc ghi nhận khoản còn lại.",
                    "Khách nên giữ nguyên phụ kiện, hộp túi và tình trạng vệ sinh như khi nhận."
                ]
            },
            {
                key: "fees",
                icon: "wrench",
                eyebrow: "Phát sinh",
                title: "Hư hỏng và phí phát sinh",
                items: [
                    "Phí trả trễ, vệ sinh hoặc sửa chữa được ghi nhận theo thực tế khi hoàn trả.",
                    "Nếu thiết bị hư hỏng, YCI sẽ kiểm tra mức độ và thông báo chi phí xử lý cho khách.",
                    "Các khoản phát sinh sẽ được cộng vào phần thanh toán còn lại của đơn thuê."
                ]
            },
            {
                key: "payment",
                icon: "wallet-cards",
                eyebrow: "Cọc và thanh toán",
                title: "Cọc và thanh toán",
                items: [
                    "Tiền cọc hiển thị trên website là mức tham khảo; mức thực thu do nhân viên áp dụng khi xác nhận.",
                    "Thanh toán và số tiền còn lại được theo dõi trong lịch sử đơn thuê của khách.",
                    "Sau khi trả máy và không có phát sinh, khoản cọc được đối soát theo quy định cửa hàng."
                ]
            }
        ];

        function ruleCard(group, items, isFallback = false) {
            const lines = items && items.length ? items : group.items;
            return `
                <article class="rule-card">
                    <div class="rule-card-head">
                        <span class="rule-icon"><i data-lucide="${group.icon}"></i></span>
                        <div>
                            <p class="eyebrow">${YCI.escapeHtml(group.eyebrow)}</p>
                            <h2>${YCI.escapeHtml(group.title)}</h2>
                        </div>
                    </div>
                    <div class="rule-list">
                        ${lines.map((line, index) => `<div class="rule-item"><strong>${index + 1}.</strong><span>${YCI.escapeHtml(line)}</span></div>`).join("")}
                    </div>
                    ${isFallback ? `<p class="field-note">Nội dung tạm hiển thị khi backend chưa có dữ liệu cho nhóm này.</p>` : ""}
                </article>
            `;
        }

        Promise.all([
            YCI.apiGet("/api/admin/rental-regulation"),
            YCI.apiGet("/api/admin/schedule-keeping-regulation")
        ])
            .then(([rentalResponse, scheduleResponse]) => {
                const rentalItems = YCI.unwrapList(rentalResponse).map((item) => item.regulation).filter(Boolean);
                const scheduleItems = YCI.unwrapList(scheduleResponse).map((item) => item.scheduleKeepingRegulation).filter(Boolean);
                const board = YCI.qs("[data-regulation-board]");
                board.innerHTML = fallbackGroups.map((group) => {
                    if (group.key === "rental") {
                        return ruleCard(group, rentalItems, rentalItems.length === 0);
                    }
                    if (group.key === "schedule") {
                        return ruleCard(group, scheduleItems, scheduleItems.length === 0);
                    }
                    return ruleCard(group, group.items, true);
                }).join("");
                YCI.iconRefresh();
            })
            .catch((error) => YCI.toast(error.message, "error"));
    }

    function bootBooking() {
        const cartNode = YCI.qs("[data-booking-cart]");
        const emptyNode = YCI.qs("[data-empty-cart]");
        const totalNode = YCI.qs("[data-booking-total]");
        const depositReferenceNode = YCI.qs("[data-booking-deposit-reference]");
        const durationNote = YCI.qs("[data-duration-note]");
        const rentalSelect = YCI.qs("[data-rental-type-select]");
        const form = YCI.qs("[data-booking-form]");
        const endInput = form.elements.timeEndInput;
        let products = [];
        let selectedProducts = [];
        let rentalTypes = [];

        function selectedRentalLabel() {
            return rentalSelect.options[rentalSelect.selectedIndex]?.textContent || "";
        }

        function selectedRentalType() {
            return rentalTypes.find((item) => String(item.idRentalType) === String(rentalSelect.value));
        }

        function syncEndTime() {
            const startValue = form.elements.timeStartInput.value;
            const rentalType = selectedRentalType();
            const hours = YCI.rentalDurationHours(rentalType?.type || selectedRentalLabel());
            if (!startValue || !hours) {
                endInput.value = "";
                durationNote.textContent = !startValue
                    ? "Chọn giờ bắt đầu để hệ thống tự tính giờ trả."
                    : "Gói thuê này chưa có thời lượng rõ ràng. Hãy cấu hình loại giá dạng 4H, 6H hoặc 24H.";
                return false;
            }
            endInput.value = YCI.addHoursToLocalValue(startValue, hours);
            durationNote.textContent = `Giờ trả được tự tính sau ${YCI.formatDuration(hours)} theo gói ${selectedRentalLabel()}.`;
            return true;
        }

        function productPrice(product) {
            const label = selectedRentalLabel();
            const price = (product.rentalPriceDTOS || []).find((item) => item.rentalType === label);
            return price ? Number(price.price || 0) : null;
        }

        function rentalTotal() {
            return selectedProducts.reduce((sum, product) => sum + Number(productPrice(product) || 0), 0);
        }

        function depositReferenceTotal() {
            return selectedProducts.reduce((sum, product) => sum + Number(product.depositPrice || 0), 0);
        }

        function renderCart() {
            const ids = YCI.readCart();
            selectedProducts = ids.map((id) => products.find((product) => Number(product.idProduct) === Number(id))).filter(Boolean);
            emptyNode.hidden = selectedProducts.length > 0;
            cartNode.innerHTML = selectedProducts.map((product) => {
                const price = productPrice(product);
                return `
                    <article class="booking-item">
                        <div class="booking-item-main">
                            <img src="${YCI.productImage(product)}" alt="${YCI.escapeHtml(product.nameProduct)}" ${YCI.imageFallbackAttribute()}>
                            <div>
                                <strong>${YCI.escapeHtml(product.nameProduct)}</strong>
                                <p class="muted">${YCI.escapeHtml(product.categoryName || "")} - ${YCI.escapeHtml(product.brand || "")}</p>
                                <span class="price-chip">${price == null ? "Chưa có giá cho gói này" : YCI.money(price)}</span>
                                <span class="price-chip subtle">Cọc tham chiếu ${YCI.money(product.depositPrice)}</span>
                            </div>
                            <button class="icon-button danger" type="button" data-remove-cart="${product.idProduct}" aria-label="Xóa thiết bị">
                                <i data-lucide="x"></i>
                            </button>
                        </div>
                    </article>
                `;
            }).join("");
            totalNode.textContent = YCI.money(rentalTotal());
            depositReferenceNode.textContent = YCI.money(depositReferenceTotal());
            YCI.iconRefresh();
        }

        cartNode.addEventListener("click", async (event) => {
            const button = event.target.closest("[data-remove-cart]");
            if (!button) {
                return;
            }
            await YCI.removeFromCart(button.dataset.removeCart);
            renderCart();
        });
        YCI.qs("[data-clear-cart]").addEventListener("click", async () => {
            await YCI.clearCart();
            renderCart();
        });
        rentalSelect.addEventListener("change", () => {
            syncEndTime();
            renderCart();
        });
        form.elements.timeStartInput.addEventListener("change", syncEndTime);

        Promise.all([loadProducts(), YCI.apiGet("/api/admin/rental-type"), YCI.loadCart(true)])
            .then(([productList, rentalTypeResponse]) => {
                products = productList;
                rentalTypes = YCI.unwrapList(rentalTypeResponse);
                YCI.setOptions(rentalSelect, rentalTypes, (item) => item.idRentalType, (item) => item.type, "Chọn loại giá");
                syncEndTime();
                renderCart();
            })
            .catch((error) => YCI.toast(error.message, "error"));

        form.addEventListener("submit", async (event) => {
            event.preventDefault();
            if (!selectedProducts.length) {
                YCI.toast("Cần chọn ít nhất một thiết bị.", "error");
                return;
            }
            if (!syncEndTime()) {
                YCI.toast("Vui lòng chọn giờ bắt đầu và gói thuê có thời lượng rõ ràng.", "error");
                return;
            }
            const missingPriceProducts = selectedProducts.filter((product) => productPrice(product) == null);
            if (missingPriceProducts.length) {
                YCI.toast("Có thiết bị chưa có giá cho gói thuê đã chọn.", "error");
                return;
            }
            const formData = new FormData();
            formData.append("timeStart", YCI.toBackendDate(form.elements.timeStartInput.value));
            formData.append("timeEnd", YCI.toBackendDate(endInput.value));
            formData.append("note", form.elements.note.value || "");
            formData.append("userId", form.elements.userId.value);
            formData.append("totalAmount", String(rentalTotal()));
            formData.append("idRentalType", form.elements.idRentalType.value);
            selectedProducts.forEach((product) => formData.append("productIds", product.idProduct));
            formData.append("documentType", form.elements.documentType.value);
            formData.append("documentNumber", form.elements.documentNumber.value);
            formData.append("imageFront", form.elements.imageFront.files[0]);
            formData.append("imageBack", form.elements.imageBack.files[0]);
            try {
                await YCI.apiForm("/api/booking", "POST", formData);
                await YCI.clearCart();
                YCI.toast("Đã gửi yêu cầu thuê.", "success");
                window.setTimeout(() => window.location.assign("/orders"), 700);
            } catch (error) {
                YCI.toast(error.message, "error");
            }
        });
    }

    function bootProfile() {
        const profileForm = YCI.qs("[data-profile-form]");
        const passwordForm = YCI.qs("[data-password-form]");
        profileForm?.addEventListener("submit", async (event) => {
            event.preventDefault();
            try {
                await YCI.apiJson("/api/profile", "PUT", YCI.formObject(profileForm));
                YCI.toast("Đã cập nhật hồ sơ.", "success");
            } catch (error) {
                YCI.toast(error.message, "error");
            }
        });
        passwordForm?.addEventListener("submit", async (event) => {
            event.preventDefault();
            try {
                await YCI.apiJson("/api/password", "PUT", YCI.formObject(passwordForm));
                passwordForm.reset();
                YCI.toast("Đã cập nhật mật khẩu.", "success");
            } catch (error) {
                YCI.toast(error.message, "error");
            }
        });
    }

    function bootOrders() {
        const user = window.YCI_SESSION?.user;
        if (!user?.idUser) {
            return;
        }
        YCI.apiGet(`/api/booking/id-user=${user.idUser}`)
            .then((response) => {
                const bookings = YCI.unwrapList(response);
                const node = YCI.qs("[data-customer-orders]");
                if (!bookings.length) {
                    node.innerHTML = `<div class="surface empty-state"><i data-lucide="clipboard-list"></i><h3>Chưa có đơn thuê</h3><p>Đơn thuê mới sẽ xuất hiện tại đây sau khi gửi yêu cầu.</p></div>`;
                    YCI.iconRefresh();
                    return;
                }
                node.innerHTML = bookings.map((booking) => {
                    const summary = YCI.bookingSummary(booking);
                    const products = (booking.bookingDetailDTOS || []).map((detail) => detail.productBookingDTO?.nameProduct).filter(Boolean).join(", ");
                    const canCancel = ["WAITING ACCEPT", "WAITING_ACCEPT", "PENDING", "CHỜ XÁC NHẬN"].includes(String(booking.status || "").toUpperCase());
                    return `
                        <article class="surface order-card">
                            <div class="order-head">
                                <div>
                                    <p class="eyebrow">Đơn #${booking.idBooking}</p>
                                    <h2>${YCI.escapeHtml(products || "Đơn thuê thiết bị")}</h2>
                                    <p class="muted">${YCI.formatDate(booking.timeStart)} - ${YCI.formatDate(booking.timeEnd)}</p>
                                </div>
                                <span class="status-pill ${YCI.statusClass(booking.status)}">${YCI.escapeHtml(YCI.statusLabel(booking.status))}</span>
                            </div>
                            <div class="info-grid">
                                <div class="info-card"><strong>${YCI.money(booking.totalAmount)}</strong><p class="muted">Tổng giá trị</p></div>
                                <div class="info-card"><strong>${YCI.money(summary.paid)}</strong><p class="muted">Đã thanh toán/cọc</p></div>
                                <div class="info-card"><strong>${YCI.money(summary.remain)}</strong><p class="muted">Còn lại</p></div>
                            </div>
                            ${canCancel ? `<button class="button ghost" type="button" data-cancel-booking="${booking.idBooking}">Hủy đơn chờ xác nhận</button>` : ""}
                        </article>
                    `;
                }).join("");
                node.addEventListener("click", async (event) => {
                    const button = event.target.closest("[data-cancel-booking]");
                    if (!button) {
                        return;
                    }
                    try {
                        await YCI.apiJson(`/api/booking/id=${button.dataset.cancelBooking}/cancel?userId=${user.idUser}`, "PUT", {});
                        YCI.toast("Đã hủy đơn thuê.", "success");
                        window.setTimeout(() => window.location.reload(), 500);
                    } catch (error) {
                        YCI.toast(error.message, "error");
                    }
                });
            })
            .catch((error) => YCI.toast(error.message, "error"));
    }

    document.addEventListener("DOMContentLoaded", () => {
        const page = document.body.dataset.page;
        if (page === "home") bootHome();
        if (page === "catalog") bootCatalog();
        if (page === "product-detail") bootProductDetail();
        if (page === "pricing") bootPricing();
        if (page === "regulations") bootRegulations();
        if (page === "booking") bootBooking();
        if (page === "profile") bootProfile();
        if (page === "orders") bootOrders();
    });
})();
