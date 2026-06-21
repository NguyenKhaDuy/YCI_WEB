(function () {
    async function loadPaged(url) {
        let page = 1;
        let totalPage = 1;
        const items = [];
        do {
            const separator = url.includes("?") ? "&" : "?";
            const response = await YCI.apiGet(`${url}${separator}page=${page}`);
            items.push(...YCI.unwrapList(response));
            totalPage = Number(response.totalPage || 1);
            page += 1;
        } while (page <= totalPage);
        return items;
    }

    function docImage(raw) {
        if (!raw) {
            return YCI.FALLBACK_IMAGE;
        }
        return raw.startsWith("data:") ? raw : `data:image/jpeg;base64,${raw}`;
    }

    function financeOf(booking) {
        const payments = booking.paymentBookingDTOS || [];
        const paid = payments.reduce((sum, payment) => sum + Number(payment.price || 0), 0);
        const lastPayment = payments[payments.length - 1];
        const remain = lastPayment ? Number(lastPayment.remainAmount || 0) : Number(booking.totalAmount || 0);
        return { paid, remain };
    }

    async function adminResources() {
        const [products, bookings, users, statuses, deposits, payments, rentalTypes, categories] = await Promise.all([
            YCI.apiGet("/api/product/all").then(YCI.unwrapList),
            loadPaged("/api/admin/booking"),
            loadPaged("/api/user"),
            YCI.apiGet("/api/admin/status").then(YCI.unwrapList),
            YCI.apiGet("/api/deposit-type").then(YCI.unwrapList),
            YCI.apiGet("/api/payment-method").then(YCI.unwrapList),
            YCI.apiGet("/api/admin/rental-type").then(YCI.unwrapList),
            YCI.apiGet("/api/category").then(YCI.unwrapList)
        ]);
        return { products, bookings, users, statuses, deposits, payments, rentalTypes, categories };
    }

    function bootDashboard() {
        adminResources()
            .then(({ products, bookings, users }) => {
                const paid = bookings.reduce((sum, booking) => sum + financeOf(booking).paid, 0);
                YCI.qs("[data-admin-products]").textContent = products.length;
                YCI.qs("[data-admin-bookings]").textContent = bookings.length;
                YCI.qs("[data-admin-users]").textContent = users.length;
                YCI.qs("[data-admin-revenue]").textContent = YCI.money(paid);

                YCI.qs("[data-dashboard-bookings]").innerHTML = bookings.slice(0, 6).map((booking) => `
                    <a class="compact-item" href="/admin/bookings">
                        <div>
                            <strong>Đơn #${booking.idBooking} - ${YCI.escapeHtml(booking.userBookingDTO?.fullName || "Khách hàng")}</strong>
                            <p>${YCI.formatDate(booking.timeStart)} - ${YCI.money(booking.totalAmount)}</p>
                        </div>
                        <span class="status-pill ${YCI.statusClass(booking.status)}">${YCI.escapeHtml(YCI.statusLabel(booking.status))}</span>
                    </a>
                `).join("") || `<p class="muted">Chưa có đơn thuê.</p>`;

                const grouped = products.reduce((acc, product) => {
                    const key = product.status || "Chưa rõ";
                    acc[key] = (acc[key] || 0) + 1;
                    return acc;
                }, {});
                YCI.qs("[data-dashboard-status]").innerHTML = Object.entries(grouped).map(([status, count]) => `
                    <div class="compact-item">
                        <div>
                            <strong>${YCI.escapeHtml(YCI.statusLabel(status))}</strong>
                            <p>${count} thiết bị</p>
                        </div>
                        <span class="status-pill ${YCI.statusClass(status)}">${count}</span>
                    </div>
                `).join("") || `<p class="muted">Chưa có dữ liệu thiết bị.</p>`;
                YCI.iconRefresh();
            })
            .catch((error) => YCI.toast(error.message, "error"));
    }

    function bootAdminBookings() {
        const state = {
            bookings: [],
            statuses: [],
            deposits: [],
            payments: [],
            search: "",
            status: "",
            selected: null
        };
        const table = YCI.qs("[data-admin-bookings-table]");
        const inspector = YCI.qs("[data-booking-inspector]");
        const statusFilter = YCI.qs("[data-admin-booking-status]");
        const search = YCI.qs("[data-admin-booking-search]");
        const statusTabs = YCI.qs("[data-booking-status-tabs]");
        const readOnly = false;

        async function load() {
            const [bookings, statuses, deposits, payments] = await Promise.all([
                loadPaged("/api/admin/booking"),
                YCI.apiGet("/api/admin/status").then(YCI.unwrapList),
                YCI.apiGet("/api/deposit-type").then(YCI.unwrapList),
                YCI.apiGet("/api/payment-method").then(YCI.unwrapList)
            ]);
            state.bookings = bookings;
            state.statuses = statuses;
            state.deposits = deposits;
            state.payments = payments;
            YCI.setOptions(statusFilter, statuses, (item) => item.statusCode, (item) => YCI.statusLabel(item.statusCode), "Tất cả trạng thái");
            render();
        }

        function filteredBookings() {
            const keyword = state.search.trim().toLowerCase();
            return state.bookings.filter((booking) => {
                const customer = booking.userBookingDTO || {};
                const products = (booking.bookingDetailDTOS || []).map((detail) => detail.productBookingDTO?.nameProduct).join(" ");
                const text = YCI.cleanText([booking.idBooking, customer.fullName, customer.phone, customer.email, products].join(" ")).toLowerCase();
                const matchSearch = !keyword || text.includes(keyword);
                const matchStatus = !state.status || booking.status === state.status;
                return matchSearch && matchStatus;
            });
        }

        function render() {
            renderStatusTabs();
            table.innerHTML = filteredBookings().map((booking) => `
                <tr data-booking-row="${booking.idBooking}">
                    <td><strong>#${booking.idBooking}</strong><br><span class="muted">${YCI.formatDate(booking.createdAt)}</span></td>
                    <td>${YCI.escapeHtml(booking.userBookingDTO?.fullName || "-")}<br><span class="muted">${YCI.escapeHtml(booking.userBookingDTO?.phone || "")}</span></td>
                    <td>${YCI.formatDate(booking.timeStart)}<br><span class="muted">${YCI.formatDate(booking.timeEnd)}</span></td>
                    <td>${YCI.money(booking.totalAmount)}</td>
                    <td><span class="status-pill ${YCI.statusClass(booking.status)}">${YCI.escapeHtml(YCI.statusLabel(booking.status))}</span></td>
                </tr>
            `).join("") || `<tr><td colspan="5">Không có đơn thuê phù hợp.</td></tr>`;
            if (state.selected) {
                const latest = state.bookings.find((booking) => booking.idBooking === state.selected.idBooking);
                state.selected = latest || null;
                renderInspector();
            }
            YCI.iconRefresh();
        }

        function renderStatusTabs() {
            if (!statusTabs) {
                return;
            }
            const counts = state.bookings.reduce((acc, booking) => {
                const key = booking.status || "";
                acc[key] = (acc[key] || 0) + 1;
                return acc;
            }, {});
            const items = [
                { value: "", label: "Tất cả", count: state.bookings.length },
                ...Object.entries(counts).map(([value, count]) => ({
                    value,
                    label: YCI.statusLabel(value),
                    count
                }))
            ];
            statusTabs.innerHTML = items.map((item) => `
                <button class="admin-tab ${String(state.status) === String(item.value) ? "is-active" : ""}"
                        type="button"
                        data-booking-status-tab="${YCI.escapeHtml(item.value)}">
                    <span>${YCI.escapeHtml(item.label)}</span>
                    <strong>${item.count}</strong>
                </button>
            `).join("");
        }

        function options(items, valueKey, labelKey) {
            return items.map((item) => {
                const label = labelKey === "statusCode" ? YCI.statusLabel(item[labelKey]) : item[labelKey];
                return `<option value="${item[valueKey]}">${YCI.escapeHtml(label)}</option>`;
            }).join("");
        }

        function depositOptions() {
            return state.deposits.map((item) => {
                const percent = Number(item.percentDeposit || 0);
                return `<option value="${item.idDepositType}">${YCI.escapeHtml(item.type)} (${percent}% giá thuê)</option>`;
            }).join("");
        }

        function updateDepositPreview(form, booking) {
            if (!form || !booking) {
                return;
            }
            const preview = form.querySelector("[data-accept-deposit-preview]");
            const deposit = state.deposits.find((item) => String(item.idDepositType) === String(form.elements.depositTypeId.value));
            const percent = Number(deposit?.percentDeposit || 0);
            const total = Number(booking.totalAmount || 0);
            const depositAmount = total * percent / 100;
            preview.innerHTML = `
                <span>Cọc cần thu (${percent}%)</span>
                <strong>${YCI.money(depositAmount)}</strong>
                <small>Còn lại sau cọc: ${YCI.money(total - depositAmount)}</small>
            `;
        }

        function renderInspector() {
            const booking = state.selected;
            if (!booking) {
                inspector.innerHTML = `
                    <div class="empty-state small">
                        <i data-lucide="mouse-pointer-click"></i>
                        <h3>Chọn một đơn thuê</h3>
                        <p>Chi tiết khách hàng, giấy tờ, cọc và thiết bị sẽ hiển thị tại đây.</p>
                    </div>
                `;
                YCI.iconRefresh();
                return;
            }
            const customer = booking.userBookingDTO || {};
            const document = booking.documentBookingDTO || {};
            const detailOptions = (booking.bookingDetailDTOS || []).map((detail) => `
                <option value="${detail.idBookingDetail}">${YCI.escapeHtml(detail.productBookingDTO?.nameProduct || `Chi tiết #${detail.idBookingDetail}`)}</option>
            `).join("");
            const payments = booking.paymentBookingDTOS || [];
            const finance = financeOf(booking);
            inspector.innerHTML = `
                <div>
                    <p class="eyebrow">Đơn #${booking.idBooking}</p>
                    <h2>${YCI.escapeHtml(customer.fullName || "Khách hàng")}</h2>
                    <p class="muted">${YCI.escapeHtml(customer.phone || "")} - ${YCI.escapeHtml(customer.email || "")}</p>
                </div>
                <div class="info-grid">
                    <div class="info-card"><strong>${YCI.money(booking.totalAmount)}</strong><p class="muted">Tổng đơn</p></div>
                    <div class="info-card"><strong>${YCI.money(finance.paid)}</strong><p class="muted">Đã thu</p></div>
                    <div class="info-card"><strong>${YCI.money(finance.remain)}</strong><p class="muted">Còn lại</p></div>
                </div>
                <div class="compact-list">
                    ${(booking.bookingDetailDTOS || []).map((detail) => `
                        <div class="compact-item">
                            <div>
                                <strong>${YCI.escapeHtml(detail.productBookingDTO?.nameProduct || "Thiết bị")}</strong>
                                <p>${YCI.escapeHtml(detail.productBookingDTO?.serialNumber || "")} - ${YCI.money(detail.totalPrice)}</p>
                            </div>
                        </div>
                    `).join("")}
                </div>
                <div class="surface">
                    <h3>Giấy tờ thế chấp</h3>
                    <p class="muted">${YCI.escapeHtml(document.documentType || "-")} - ${YCI.escapeHtml(document.documentNumber || "-")}</p>
                    <div class="product-grid">
                        <img src="${docImage(document.imageFront)}" alt="Giấy tờ mặt trước">
                        <img src="${docImage(document.imageBack)}" alt="Giấy tờ mặt sau">
                    </div>
                    <form class="form-grid" data-document-status-form>
                        <input type="hidden" name="documentId" value="${document.idDocument || ""}">
                    <label><span>Trạng thái giấy tờ</span><select name="statusId" required>${options(state.statuses, "idStatus", "statusCode")}</select></label>
                        <button class="button ghost" type="submit">Cập nhật giấy tờ</button>
                    </form>
                </div>
                <form class="surface form-grid" data-accept-booking-form>
                    <h3>Xác nhận đơn, lập hợp đồng và ghi nhận cọc</h3>
                    <input type="hidden" name="bookingId" value="${booking.idBooking}">
                    <label><span>Loại tiền cọc</span><select name="depositTypeId" required>${depositOptions()}</select></label>
                    <label><span>Phương thức thanh toán</span><select name="paymentMethodId" required>${options(state.payments, "idPaymentsMethod", "method")}</select></label>
                    <div class="total-panel secondary" data-accept-deposit-preview></div>
                    <button class="button primary" type="submit">Xác nhận đơn</button>
                </form>
                <div class="handover-checklist">
                    <span><i data-lucide="scan-line"></i> Đối chiếu serial và phụ kiện trước khi giao</span>
                    <span><i data-lucide="shield-check"></i> Kiểm tra giấy tờ thế chấp và tình trạng thiết bị</span>
                    <span><i data-lucide="receipt"></i> Ghi nhận cọc, phí phát sinh và thanh toán còn lại</span>
                </div>
                <form class="surface form-grid" data-booking-status-form>
                    <h3>Cập nhật trạng thái đơn</h3>
                    <input type="hidden" name="bookingId" value="${booking.idBooking}">
                    <label><span>Trạng thái</span><select name="statusId" required>${options(state.statuses, "idStatus", "statusCode")}</select></label>
                    <button class="button ghost" type="submit">Cập nhật đơn</button>
                </form>
                <form class="surface form-grid" data-return-form>
                    <h3>Ghi nhận trả thiết bị và phí phát sinh</h3>
                    <label><span>Thiết bị trả</span><select name="bookingDetailId" required>${detailOptions}</select></label>
                    <div class="form-row two">
                        <label><span>Số lượng trả</span><input name="returnedQuantity" type="number" min="1" value="1" required></label>
                        <label><span>Giờ trễ</span><input name="lateHours" type="number" min="0" value="0" required></label>
                    </div>
                    <div class="form-row two">
                        <label><span>Phí trễ</span><input name="lateFee" type="number" min="0" value="0" required></label>
                        <label><span>Phí vệ sinh</span><input name="cleaningFee" type="number" min="0" value="0" required></label>
                    </div>
                    <div class="form-row two">
                        <label><span>Phí sửa chữa</span><input name="damageFee" type="number" min="0" value="0" required></label>
                        <label><span>Tổng phí</span><input name="subtotalFee" type="number" min="0" value="0" required></label>
                    </div>
                    <label><span>Tình trạng sản phẩm sau trả</span><select name="statusId" required>${options(state.statuses, "idStatus", "statusCode")}</select></label>
                    <label><span>Ghi chú</span><textarea name="description" rows="3"></textarea></label>
                    <button class="button primary" type="submit">Lưu trả thiết bị</button>
                </form>
                <div class="surface">
                    <h3>Lịch sử thanh toán</h3>
                    <div class="compact-list">
                        ${payments.map((payment) => `
                            <div class="compact-item">
                                <div>
                                    <strong>${YCI.money(payment.price)}</strong>
                                    <p>${YCI.escapeHtml(payment.paymentMethod || "")} - ${YCI.formatDate(payment.paidAt)}</p>
                                </div>
                                <span class="price-chip">Còn ${YCI.money(payment.remainAmount)}</span>
                            </div>
                        `).join("") || `<p class="muted">Chưa ghi nhận thanh toán.</p>`}
                    </div>
                </div>
            `;
            if (readOnly) {
                inspector.querySelectorAll("form").forEach((form) => form.remove());
                inspector.insertAdjacentHTML("afterbegin", `
                    <div class="view-only-note">
                        <i data-lucide="eye"></i>
                        <span>Quản trị viên chỉ xem thông tin đơn, không thực hiện xác nhận hoặc trả thiết bị tại màn này.</span>
                    </div>
                `);
            }
            updateDepositPreview(inspector.querySelector("[data-accept-booking-form]"), booking);
            YCI.iconRefresh();
        }

        table.addEventListener("click", (event) => {
            const row = event.target.closest("[data-booking-row]");
            if (!row) {
                return;
            }
            state.selected = state.bookings.find((booking) => Number(booking.idBooking) === Number(row.dataset.bookingRow));
            renderInspector();
        });
        search?.addEventListener("input", () => {
            state.search = search.value;
            render();
        });
        statusFilter?.addEventListener("change", () => {
            state.status = statusFilter.value;
            render();
        });
        statusTabs?.addEventListener("click", (event) => {
            const button = event.target.closest("[data-booking-status-tab]");
            if (!button) {
                return;
            }
            state.status = button.dataset.bookingStatusTab || "";
            if (statusFilter) {
                statusFilter.value = state.status;
            }
            render();
        });
        YCI.qs("[data-refresh-admin-bookings]")?.addEventListener("click", () => load().catch((error) => YCI.toast(error.message, "error")));

        inspector.addEventListener("change", (event) => {
            const select = event.target.closest("[data-accept-booking-form] select[name='depositTypeId']");
            if (!select) {
                return;
            }
            updateDepositPreview(select.closest("[data-accept-booking-form]"), state.selected);
        });

        inspector.addEventListener("submit", async (event) => {
            event.preventDefault();
            if (readOnly) {
                YCI.toast("Quản trị viên chỉ xem thông tin đơn tại màn này.", "error");
                return;
            }
            const form = event.target;
            try {
                if (form.matches("[data-accept-booking-form]")) {
                    await YCI.apiJson("/api/admin/booking/accept", "PUT", YCI.formObject(form));
                    YCI.toast("Đã xác nhận đơn.", "success");
                }
                if (form.matches("[data-booking-status-form]")) {
                    await YCI.apiJson("/api/admin/booking/status", "PUT", YCI.formObject(form));
                    YCI.toast("Đã cập nhật trạng thái đơn.", "success");
                }
                if (form.matches("[data-document-status-form]")) {
                    await YCI.apiJson("/api/admin/document", "PUT", YCI.formObject(form));
                    YCI.toast("Đã cập nhật giấy tờ.", "success");
                }
                if (form.matches("[data-return-form]")) {
                    const payload = YCI.formObject(form);
                    ["returnedQuantity", "lateHours", "lateFee", "cleaningFee", "damageFee", "subtotalFee", "bookingDetailId", "statusId"].forEach((key) => {
                        payload[key] = Number(payload[key] || 0);
                    });
                    await YCI.apiJson("/api/admin/return-detail", "POST", payload);
                    YCI.toast("Đã ghi nhận trả thiết bị.", "success");
                }
                await load();
            } catch (error) {
                YCI.toast(error.message, "error");
            }
        });

        load().catch((error) => YCI.toast(error.message, "error"));
    }

    function bootAdminProducts() {
        const state = { products: [], categories: [], rentalTypes: [], search: "", category: "", status: "" };
        const table = YCI.qs("[data-admin-products-table]");
        const productForm = YCI.qs("[data-admin-product-form]");
        const priceForm = YCI.qs("[data-admin-price-form]");
        const productSearch = YCI.qs("[data-admin-product-search]");
        const categoryFilter = YCI.qs("[data-admin-product-category-filter]");
        const statusFilter = YCI.qs("[data-admin-product-status-filter]");

        async function load() {
            const [products, categories, rentalTypes] = await Promise.all([
                YCI.apiGet("/api/product/all").then(YCI.unwrapList),
                YCI.apiGet("/api/category").then(YCI.unwrapList),
                YCI.apiGet("/api/admin/rental-type").then(YCI.unwrapList)
            ]);
            state.products = products;
            state.categories = categories;
            state.rentalTypes = rentalTypes;
            YCI.setOptions(YCI.qs("[data-admin-product-category]"), categories, (item) => item.idCategory, (item) => item.nameCategory, "Chọn danh mục");
            YCI.setOptions(categoryFilter, categories, (item) => item.idCategory, (item) => item.nameCategory, "Tất cả danh mục");
            if (categoryFilter) {
                categoryFilter.value = state.category;
            }
            YCI.setOptions(YCI.qs("[data-price-product-select]"), products, (item) => item.idProduct, (item) => item.nameProduct, "Chọn thiết bị");
            YCI.setOptions(YCI.qs("[data-price-type-select]"), rentalTypes, (item) => item.idRentalType, (item) => item.type, "Chọn loại giá");
            renderStatusFilter();
            render();
        }

        function renderStatusFilter() {
            if (!statusFilter) {
                return;
            }
            const statuses = Array.from(new Set(state.products.map((product) => product.status).filter(Boolean)));
            statusFilter.innerHTML = `<option value="">Tất cả tình trạng</option>${statuses.map((status) => `
                <option value="${YCI.escapeHtml(status)}">${YCI.escapeHtml(YCI.statusLabel(status))}</option>
            `).join("")}`;
            statusFilter.value = state.status;
        }

        function filteredProducts() {
            const keyword = state.search.trim().toLowerCase();
            return state.products.filter((product) => {
                const text = YCI.cleanText([
                    product.nameProduct,
                    product.brand,
                    product.serialNumber,
                    product.categoryName,
                    product.description,
                    YCI.statusLabel(product.status)
                ].join(" ")).toLowerCase();
                const matchSearch = !keyword || text.includes(keyword);
                const matchCategory = !state.category || String(product.categoryId) === String(state.category);
                const matchStatus = !state.status || String(product.status || "") === String(state.status);
                return matchSearch && matchCategory && matchStatus;
            });
        }

        function render() {
            const products = filteredProducts();
            table.innerHTML = products.map((product) => `
                <tr>
                    <td><strong>${YCI.escapeHtml(product.nameProduct)}</strong><br><span class="muted">${YCI.escapeHtml(product.brand || "")} - ${YCI.escapeHtml(product.serialNumber || "")}</span></td>
                    <td>${YCI.escapeHtml(product.categoryName || "-")}</td>
                    <td>${(product.rentalPriceDTOS || []).map((price) => `
                        <span class="price-chip">
                            ${YCI.escapeHtml(price.rentalType)} ${YCI.money(price.price)}
                            <button class="chip-action" type="button" data-edit-price="${price.idRentalPrice}" data-product-id="${product.idProduct}" aria-label="Sửa giá">Sửa</button>
                            <button class="chip-action danger" type="button" data-delete-price="${price.idRentalPrice}" aria-label="Xóa giá">Xóa</button>
                        </span>`).join(" ") || "Chưa có giá"}</td>
                    <td><span class="status-pill ${YCI.statusClass(product.status)}">${YCI.escapeHtml(YCI.statusLabel(product.status))}</span></td>
                    <td>
                        <div class="button-row">
                            <button class="icon-button" type="button" data-edit-product="${product.idProduct}" aria-label="Sửa thiết bị"><i data-lucide="pencil"></i></button>
                            <button class="icon-button danger" type="button" data-delete-product="${product.idProduct}" aria-label="Xóa thiết bị"><i data-lucide="trash-2"></i></button>
                        </div>
                    </td>
                </tr>
            `).join("") || `<tr><td colspan="5">Không có thiết bị phù hợp.</td></tr>`;
            YCI.iconRefresh();
        }

        function resetProductForm() {
            productForm.reset();
            productForm.elements.idProduct.value = "";
        }

        table.addEventListener("click", async (event) => {
            const edit = event.target.closest("[data-edit-product]");
            const remove = event.target.closest("[data-delete-product]");
            const editPrice = event.target.closest("[data-edit-price]");
            const deletePrice = event.target.closest("[data-delete-price]");
            if (edit) {
                const product = state.products.find((item) => Number(item.idProduct) === Number(edit.dataset.editProduct));
                if (!product) return;
                productForm.elements.idProduct.value = product.idProduct || "";
                productForm.elements.nameProduct.value = YCI.cleanText(product.nameProduct || "");
                productForm.elements.brand.value = YCI.cleanText(product.brand || "");
                productForm.elements.serialNumber.value = YCI.cleanText(product.serialNumber || "");
                productForm.elements.idCategory.value = product.categoryId || "";
                productForm.elements.depositPrice.value = product.depositPrice || 0;
                productForm.elements.description.value = YCI.cleanText(product.description || "");
            }
            if (remove) {
                try {
                    await YCI.request(`/api/admin/product/id-product=${remove.dataset.deleteProduct}`, { method: "DELETE" });
                    YCI.toast("Đã xóa thiết bị.", "success");
                    await load();
                } catch (error) {
                    YCI.toast(error.message, "error");
                }
            }
            if (editPrice) {
                const product = state.products.find((item) => Number(item.idProduct) === Number(editPrice.dataset.productId));
                const price = (product?.rentalPriceDTOS || []).find((item) => Number(item.idRentalPrice) === Number(editPrice.dataset.editPrice));
                if (!product || !price) return;
                priceForm.elements.idRentalPrice.value = price.idRentalPrice || "";
                priceForm.elements.productId.value = product.idProduct || "";
                const rentalType = state.rentalTypes.find((item) => item.type === price.rentalType);
                priceForm.elements.typeId.value = rentalType?.idRentalType || "";
                priceForm.elements.price.value = price.price || 0;
                YCI.qs("[data-price-submit]").textContent = "Cập nhật giá thuê";
            }
            if (deletePrice) {
                try {
                    await YCI.request(`/api/admin/product/rental-price/id=${deletePrice.dataset.deletePrice}`, { method: "DELETE" });
                    YCI.toast("Đã xóa giá thuê.", "success");
                    await load();
                } catch (error) {
                    YCI.toast(error.message, "error");
                }
            }
        });

        productForm.addEventListener("submit", async (event) => {
            event.preventDefault();
            const id = productForm.elements.idProduct.value;
            const images = Array.from(productForm.elements.images.files || []);
            if (!id && !images.length) {
                YCI.toast("Cần chọn hình ảnh cho thiết bị mới.", "error");
                return;
            }
            const formData = new FormData();
            ["idProduct", "nameProduct", "brand", "description", "serialNumber", "depositPrice", "idCategory"].forEach((name) => {
                if (productForm.elements[name]?.value) {
                    formData.append(name, productForm.elements[name].value);
                }
            });
            images.forEach((file) => formData.append("images", file));
            try {
                await YCI.apiForm("/api/admin/product", id ? "PUT" : "POST", formData);
                YCI.toast("Đã lưu thiết bị.", "success");
                resetProductForm();
                await load();
            } catch (error) {
                YCI.toast(error.message, "error");
            }
        });

        priceForm.addEventListener("submit", async (event) => {
            event.preventDefault();
            const payload = YCI.formObject(priceForm);
            if (payload.idRentalPrice) {
                payload.idRentalPrice = Number(payload.idRentalPrice);
            } else {
                delete payload.idRentalPrice;
            }
            payload.productId = Number(payload.productId);
            payload.typeId = Number(payload.typeId);
            payload.price = Number(payload.price);
            try {
                await YCI.apiJson("/api/admin/product/rental-price", payload.idRentalPrice ? "PUT" : "POST", payload);
                YCI.toast(payload.idRentalPrice ? "Đã cập nhật giá thuê." : "Đã thêm giá thuê.", "success");
                priceForm.reset();
                YCI.qs("[data-price-submit]").textContent = "Thêm giá thuê";
                await load();
            } catch (error) {
                YCI.toast(error.message, "error");
            }
        });

        YCI.qs("[data-reset-product-form]")?.addEventListener("click", resetProductForm);
        YCI.qs("[data-reset-price-form]")?.addEventListener("click", () => {
            priceForm.reset();
            YCI.qs("[data-price-submit]").textContent = "Thêm giá thuê";
        });
        productSearch?.addEventListener("input", () => {
            state.search = productSearch.value;
            render();
        });
        categoryFilter?.addEventListener("change", () => {
            state.category = categoryFilter.value;
            render();
        });
        statusFilter?.addEventListener("change", () => {
            state.status = statusFilter.value;
            render();
        });
        load().catch((error) => YCI.toast(error.message, "error"));
    }

    function bootCustomers() {
        const state = { users: [], search: "", role: "" };
        const table = YCI.qs("[data-customers-table]");
        const input = YCI.qs("[data-customer-search]");
        const roleTabs = YCI.qs("[data-account-role-tabs]");

        function roleCounts() {
            return state.users.reduce((acc, user) => {
                const role = Number(user.role ?? 1);
                acc.total += 1;
                if (role === 0) acc.admins += 1;
                else if (role === 2) acc.staff += 1;
                else acc.customers += 1;
                return acc;
            }, { total: 0, customers: 0, staff: 0, admins: 0 });
        }

        function renderRoleTabs(counts) {
            YCI.qs("[data-account-total]") && (YCI.qs("[data-account-total]").textContent = counts.total);
            YCI.qs("[data-account-customers]") && (YCI.qs("[data-account-customers]").textContent = counts.customers);
            YCI.qs("[data-account-staff]") && (YCI.qs("[data-account-staff]").textContent = counts.staff);
            YCI.qs("[data-account-admins]") && (YCI.qs("[data-account-admins]").textContent = counts.admins);
            if (!roleTabs) {
                return;
            }
            const items = [
                { value: "", label: "Tất cả", count: counts.total },
                { value: "1", label: "Khách hàng", count: counts.customers },
                { value: "2", label: "Nhân viên", count: counts.staff },
                { value: "0", label: "Quản trị viên", count: counts.admins }
            ];
            roleTabs.innerHTML = items.map((item) => `
                <button class="admin-tab ${String(state.role) === String(item.value) ? "is-active" : ""}"
                        type="button"
                        data-account-role="${item.value}">
                    <span>${item.label}</span>
                    <strong>${item.count}</strong>
                </button>
            `).join("");
        }

        function render() {
            const counts = roleCounts();
            renderRoleTabs(counts);
            const keyword = state.search.trim().toLowerCase();
            const users = state.users.filter((user) => {
                const text = YCI.cleanText([user.fullName, user.email, user.phone, user.cccd].join(" ")).toLowerCase();
                const matchSearch = !keyword || text.includes(keyword);
                const matchRole = state.role === "" || String(Number(user.role ?? 1)) === state.role;
                return matchSearch && matchRole;
            });
            table.innerHTML = users.map((user) => `
                <tr>
                    <td><strong>${YCI.escapeHtml(user.fullName || "-")}</strong><br><span class="muted">${YCI.escapeHtml(user.address || "")}</span></td>
                    <td>${YCI.escapeHtml(user.email || "-")}<br><span class="muted">${YCI.escapeHtml(user.phone || "")}</span></td>
                    <td>${YCI.escapeHtml(user.cccd || "-")}</td>
                    <td>
                        <select class="select compact-select" data-role-user="${user.idUser}">
                            <option value="0" ${Number(user.role) === 0 ? "selected" : ""}>Quản trị viên</option>
                            <option value="1" ${Number(user.role) === 1 ? "selected" : ""}>Khách hàng</option>
                            <option value="2" ${Number(user.role) === 2 ? "selected" : ""}>Nhân viên</option>
                        </select>
                    </td>
                    <td>${YCI.formatDate(user.createdAt)}</td>
                </tr>
            `).join("") || `<tr><td colspan="5">Không có khách hàng.</td></tr>`;
        }
        input?.addEventListener("input", () => {
            state.search = input.value;
            render();
        });
        roleTabs?.addEventListener("click", (event) => {
            const button = event.target.closest("[data-account-role]");
            if (!button) {
                return;
            }
            state.role = button.dataset.accountRole || "";
            render();
        });
        table.addEventListener("change", async (event) => {
            const select = event.target.closest("[data-role-user]");
            if (!select) {
                return;
            }
            try {
                await YCI.apiJson("/api/admin/user/role", "PUT", {
                    idUser: Number(select.dataset.roleUser),
                    role: Number(select.value)
                });
                YCI.toast("Đã cập nhật phân quyền.", "success");
            } catch (error) {
                YCI.toast(error.message, "error");
            }
        });
        loadPaged("/api/user")
            .then((users) => {
                state.users = users;
                render();
            })
            .catch((error) => YCI.toast(error.message, "error"));
    }

    function bootFinance() {
        Promise.all([loadPaged("/api/admin/booking"), YCI.apiGet("/api/admin/revenue/statistics")])
            .then(([bookings, statsResponse]) => {
                const stats = YCI.unwrapData(statsResponse) || {};
                const totals = bookings.reduce((acc, booking) => {
                    const finance = financeOf(booking);
                    acc.paid += finance.paid;
                    acc.remain += finance.remain;
                    acc.total += Number(booking.totalAmount || 0);
                    if ((booking.paymentBookingDTOS || []).length) {
                        acc.count += 1;
                    }
                    return acc;
                }, { paid: 0, remain: 0, total: 0, count: 0 });
                YCI.qs("[data-finance-paid]").textContent = YCI.money(stats.totalPaid ?? totals.paid);
                YCI.qs("[data-finance-remain]").textContent = YCI.money(stats.totalRemaining ?? totals.remain);
                YCI.qs("[data-finance-total]").textContent = YCI.money(stats.totalBookingAmount ?? totals.total);
                YCI.qs("[data-finance-count]").textContent = stats.paymentCount ?? totals.count;
                YCI.qs("[data-finance-table]").innerHTML = bookings.map((booking) => {
                    const finance = financeOf(booking);
                    return `
                        <tr>
                            <td><strong>#${booking.idBooking}</strong><br><span class="muted">${YCI.formatDate(booking.createdAt)}</span></td>
                            <td>${YCI.escapeHtml(booking.userBookingDTO?.fullName || "-")}</td>
                            <td>${YCI.money(finance.paid)}</td>
                            <td>${YCI.money(finance.remain)}</td>
                            <td><span class="status-pill ${YCI.statusClass(booking.status)}">${YCI.escapeHtml(YCI.statusLabel(booking.status))}</span></td>
                        </tr>
                    `;
                }).join("") || `<tr><td colspan="5">Chưa có dữ liệu thanh toán.</td></tr>`;
                const breakdown = [
                    ["Theo ngày", stats.revenueByDay || {}],
                    ["Theo tháng", stats.revenueByMonth || {}],
                    ["Theo năm", stats.revenueByYear || {}]
                ];
                YCI.qs("[data-revenue-breakdown]").innerHTML = breakdown.map(([title, values]) => `
                    <article class="info-card">
                        <h3>${title}</h3>
                        <div class="compact-list">
                            ${Object.entries(values).map(([key, value]) => `
                                <div class="compact-item"><strong>${YCI.escapeHtml(key)}</strong><span>${YCI.money(value)}</span></div>
                            `).join("") || `<p class="muted">Chưa có dữ liệu.</p>`}
                        </div>
                    </article>
                `).join("");
            })
            .catch((error) => YCI.toast(error.message, "error"));
    }

    function bootSettings() {
        const configs = [
            {
                name: "category",
                form: "[data-category-form]",
                list: "[data-category-list]",
                load: "/api/category",
                add: ["/api/admin/category", "POST"],
                update: ["/api/admin/update", "PUT"],
                remove: (item) => `/api/admin/category/idCate=${item.idCategory}`,
                id: "idCategory",
                label: "nameCategory",
                sub: "description"
            },
            {
                name: "rentalType",
                form: "[data-rental-type-form]",
                list: "[data-rental-type-list]",
                load: "/api/admin/rental-type",
                add: ["/api/admin/rental-type", "POST"],
                update: ["/api/admin/rental-type", "PUT"],
                remove: (item) => `/api/admin/rental-type/id=${item.idRentalType}`,
                id: "idRentalType",
                label: "type"
            },
            {
                name: "status",
                form: "[data-status-form]",
                list: "[data-status-list]",
                load: "/api/admin/status",
                add: ["/api/admin/status", "POST"],
                update: ["/api/admin/status", "PUT"],
                remove: (item) => `/api/admin/status/id=${item.idStatus}`,
                id: "idStatus",
                label: "statusCode"
            },
            {
                name: "deposit",
                form: "[data-deposit-form]",
                list: "[data-deposit-list]",
                load: "/api/deposit-type",
                add: ["/api/admin/deposit-type", "POST"],
                update: ["/api/admin/deposit-type", "PUT"],
                remove: (item) => `/api/admin/deposit-type/id=${item.idDepositType}`,
                id: "idDepositType",
                label: "type",
                sub: "percentDeposit"
            },
            {
                name: "rentalRegulation",
                form: "[data-rental-regulation-form]",
                list: "[data-rental-regulation-list]",
                load: "/api/admin/rental-regulation",
                add: ["/api/admin/rental-regulation", "POST"],
                update: ["/api/admin/rental-regulation", "PUT"],
                remove: (item) => `/api/admin/rental-regulation/id=${item.idRentalRegulations}`,
                id: "idRentalRegulations",
                label: "regulation"
            },
            {
                name: "schedule",
                form: "[data-schedule-form]",
                list: "[data-schedule-list]",
                load: "/api/admin/schedule-keeping-regulation",
                add: ["/api/admin/schedule-keeping-regulation", "POST"],
                update: ["/api/admin/schedule-keeping-regulation", "PUT"],
                remove: (item) => `/api/admin/schedule-keeping-regulation/id=${item.idScheduleKeepingRegulations}`,
                id: "idScheduleKeepingRegulations",
                label: "scheduleKeepingRegulation"
            }
        ];

        function payloadFrom(form) {
            const payload = YCI.formObject(form);
            Object.keys(payload).forEach((key) => {
                if (payload[key] === "") {
                    delete payload[key];
                }
                if (key.startsWith("id") || key === "percentDeposit") {
                    payload[key] = Number(payload[key]);
                }
            });
            return payload;
        }

        function setup(config) {
            const form = YCI.qs(config.form);
            const list = YCI.qs(config.list);
            let items = [];

            async function load() {
                items = YCI.unwrapList(await YCI.apiGet(config.load));
                list.innerHTML = items.map((item) => `
                    <div class="compact-item">
                        <div>
                            <strong>${YCI.escapeHtml(item[config.label] || "-")}</strong>
                            <p>${YCI.escapeHtml(config.sub ? item[config.sub] ?? "" : "")}</p>
                        </div>
                        <div class="button-row">
                            <button class="icon-button" type="button" data-config-edit="${item[config.id]}" aria-label="Sửa"><i data-lucide="pencil"></i></button>
                            <button class="icon-button danger" type="button" data-config-delete="${item[config.id]}" aria-label="Xóa"><i data-lucide="trash-2"></i></button>
                        </div>
                    </div>
                `).join("") || `<p class="muted">Chưa có dữ liệu.</p>`;
                YCI.iconRefresh();
            }

            form.addEventListener("submit", async (event) => {
                event.preventDefault();
                const payload = payloadFrom(form);
                const isUpdate = Boolean(payload[config.id]);
                const [url, method] = isUpdate ? config.update : config.add;
                try {
                    await YCI.apiJson(url, method, payload);
                    YCI.toast("Đã lưu cấu hình.", "success");
                    form.reset();
                    await load();
                } catch (error) {
                    YCI.toast(error.message, "error");
                }
            });

            list.addEventListener("click", async (event) => {
                const edit = event.target.closest("[data-config-edit]");
                const remove = event.target.closest("[data-config-delete]");
                if (edit) {
                    const item = items.find((entry) => String(entry[config.id]) === edit.dataset.configEdit);
                    if (!item) return;
                    Object.keys(item).forEach((key) => {
                        if (form.elements[key]) {
                            form.elements[key].value = item[key] ?? "";
                        }
                    });
                }
                if (remove) {
                    const item = items.find((entry) => String(entry[config.id]) === remove.dataset.configDelete);
                    if (!item) return;
                    try {
                        await YCI.request(config.remove(item), { method: "DELETE" });
                        YCI.toast("Đã xóa cấu hình.", "success");
                        await load();
                    } catch (error) {
                        YCI.toast(error.message, "error");
                    }
                }
            });

            load().catch((error) => YCI.toast(error.message, "error"));
        }

        configs.forEach(setup);
    }

    document.addEventListener("DOMContentLoaded", () => {
        const page = document.body.dataset.page;
        if (page === "admin-dashboard") bootDashboard();
        if (page === "admin-bookings" || page === "staff-bookings") bootAdminBookings();
        if (page === "admin-products") bootAdminProducts();
        if (page === "admin-customers") bootCustomers();
        if (page === "admin-finance") bootFinance();
        if (page === "admin-settings") bootSettings();
    });
})();
