import { useEffect, useMemo, useState } from "react";
import "./App.css";

const API_URL = "http://localhost:8084";

async function authFetch(url, options = {}) {
  const token = localStorage.getItem("token");
  const headers = {
    ...(options.headers || {}),
  };

  if (options.body && !headers["Content-Type"]) {
    headers["Content-Type"] = "application/json";
  }
  if (token) headers.Authorization = `Bearer ${token}`;

  return fetch(url, { ...options, headers });
}

function App() {
  const [cakes, setCakes] = useState([]);
  const [search, setSearch] = useState("");
  const [category, setCategory] = useState("All");
  const [activePage, setActivePage] = useState("home");

  const [user, setUser] = useState(() => {
    try {
      return JSON.parse(localStorage.getItem("user")) || null;
    } catch {
      return null;
    }
  });
  const [showAuth, setShowAuth] = useState(false);
  const [authMode, setAuthMode] = useState("login");

  const [cart, setCart] = useState([]);
  const [orderId, setOrderId] = useState(
    localStorage.getItem("activeOrderId") || null
  );
  const [orders, setOrders] = useState([]);
  const [myRatings, setMyRatings] = useState([]);

  const [ratingOrder, setRatingOrder] = useState(null);
  const [ratingCake, setRatingCake] = useState(null);
  const [ratingValue, setRatingValue] = useState(5);
  const [reviewText, setReviewText] = useState("");

  const [message, setMessage] = useState("");
  const [loading, setLoading] = useState(false);

  const notify = (text) => {
    setMessage(text);
    window.clearTimeout(window.__cakeMsg);
    window.__cakeMsg = window.setTimeout(() => setMessage(""), 4500);
  };

  useEffect(() => {
    loadCakes();
    if (localStorage.getItem("token")) {
      loadOrders();
      loadMyRatings();
    }
  }, []);

  useEffect(() => {
    if (user) {
      loadOrders();
      loadMyRatings();
    } else {
      setOrders([]);
      setMyRatings([]);
      setCart([]);
    }
  }, [user]);

  useEffect(() => {
    if (orderId && user) loadBasket(orderId);
  }, [orderId, user]);

  async function loadCakes() {
    try {
      const response = await fetch(`${API_URL}/cakes`);
      if (!response.ok) throw new Error("Unable to load cakes");
      const data = await response.json();
      setCakes(Array.isArray(data) ? data : []);
    } catch (error) {
      console.error(error);
      notify("Backend is not connected. Please start the API Gateway on port 8084.");
    }
  }

  async function loadOrders() {
    if (!localStorage.getItem("token")) return;
    try {
      const response = await authFetch(`${API_URL}/orders`);
      if (response.status === 401) {
        handleLogout(false);
        return;
      }
      if (!response.ok) throw new Error("Unable to load orders");
      const data = await response.json();
      const list = Array.isArray(data) ? data : [];
      setOrders(list);

      const active = list
        .filter((o) => o.status === "PLACED")
        .sort((a, b) => Number(b.id) - Number(a.id))[0];

      if (active) {
        setOrderId(String(active.id));
        localStorage.setItem("activeOrderId", String(active.id));
      } else {
        setOrderId(null);
        localStorage.removeItem("activeOrderId");
        setCart([]);
      }
    } catch (error) {
      console.error(error);
    }
  }

  async function loadBasket(id) {
    if (!id || !user) return;
    try {
      const response = await authFetch(`${API_URL}/orders/basket/${id}`);
      if (!response.ok) {
        setCart([]);
        return;
      }
      const items = await response.json();
      const list = Array.isArray(items) ? items : [];

      const enriched = list.map((item) => {
        const cake = cakes.find((c) => Number(c.id) === Number(item.cakeId));
        return {
          ...cake,
          id: cake?.id ?? item.cakeId,
          name: cake?.name ?? `Cake #${item.cakeId}`,
          price: Number(cake?.price ?? 0),
          quantity: Number(item.quantity || 1),
          basketItemId: item.id,
          cakeId: item.cakeId,
        };
      });

      setCart(enriched);
    } catch (error) {
      console.error("Basket error:", error);
    }
  }

  async function loadMyRatings() {
  if (!user) {
    setMyRatings([]);
    return;
  }

  try {
    const response = await authFetch(`${API_URL}/ratings`);

    if (!response.ok) {
      throw new Error("Unable to load ratings");
    }

    const data = await response.json();
    const ratings = Array.isArray(data) ? data : [];

    // Support both possible user object formats
    const currentUserId =
      user.id ??
      user.userId ??
      user.user_id;

    console.log("Current User ID:", currentUserId);
    console.log("All Ratings:", ratings);

    const mine = ratings.filter((rating) => {
      return Number(rating.userId) === Number(currentUserId);
    });

    console.log("My Ratings:", mine);

    setMyRatings(mine);
  } catch (error) {
    console.error("Ratings error:", error);
    setMyRatings([]);
  }
}

  async function createActiveOrder(cakeId) {
    const response = await authFetch(`${API_URL}/orders`, {
      method: "POST",
      body: JSON.stringify({
        customerName: user.name,
        cakeId: Number(cakeId),
        quantity: 1,
        totalPrice: 0,
        status: "PLACED",
      }),
    });

    if (!response.ok) {
      const text = await response.text();
      throw new Error(text || "Unable to create order");
    }

    const order = await response.json();
    setOrderId(String(order.id));
    localStorage.setItem("activeOrderId", String(order.id));
    return order.id;
  }

  async function ensureActiveOrder(cakeId) {
    const active = orders
      .filter((o) => o.status === "PLACED")
      .sort((a, b) => Number(b.id) - Number(a.id))[0];

    if (active) {
      setOrderId(String(active.id));
      localStorage.setItem("activeOrderId", String(active.id));
      return active.id;
    }

    return createActiveOrder(cakeId);
  }

  async function addToCart(cake) {
    if (!user) {
      setAuthMode("login");
      setShowAuth(true);
      notify("Please login to add cakes to your cart.");
      return;
    }

    try {
      setLoading(true);
      const currentOrderId = await ensureActiveOrder(cake.id);

      const response = await authFetch(`${API_URL}/orders/basket`, {
        method: "POST",
        body: JSON.stringify({
          orderId: Number(currentOrderId),
          cakeId: Number(cake.id),
          quantity: 1,
        }),
      });

      if (!response.ok) {
        const text = await response.text();
        throw new Error(text || "Could not add cake to cart");
      }

      await loadBasket(currentOrderId);
      await loadOrders();
      notify(`${cake.name} added to your cart.`);
    } catch (error) {
      console.error(error);
      notify(error.message || "Unable to add cake.");
    } finally {
      setLoading(false);
    }
  }

  async function updateBasketItem(item, quantity) {
    if (!item.basketItemId) return;
    if (quantity <= 0) return removeFromCart(item);

    try {
      const response = await authFetch(
        `${API_URL}/orders/basket/item/${item.basketItemId}`,
        {
          method: "PUT",
          body: JSON.stringify({
            id: item.basketItemId,
            orderId: Number(orderId),
            userId: Number(user.id),
            cakeId: Number(item.cakeId ?? item.id),
            quantity,
          }),
        }
      );

      if (!response.ok) {
        const text = await response.text();
        throw new Error(text || "Unable to update quantity");
      }

      await loadBasket(orderId);
    } catch (error) {
      console.error(error);
      notify(error.message || "Unable to update quantity.");
    }
  }

  async function increaseQuantity(item) {
    await updateBasketItem(item, Number(item.quantity) + 1);
  }

  async function decreaseQuantity(item) {
    await updateBasketItem(item, Number(item.quantity) - 1);
  }

  async function removeFromCart(item) {
    if (!item.basketItemId) return;
    try {
      const response = await authFetch(
        `${API_URL}/orders/basket/item/${item.basketItemId}`,
        { method: "DELETE" }
      );
      if (!response.ok) {
        const text = await response.text();
        throw new Error(text || "Unable to remove item");
      }
      await loadBasket(orderId);
      notify(`${item.name} removed from cart.`);
    } catch (error) {
      console.error(error);
      notify(error.message || "Unable to remove item.");
    }
  }

  async function handleCheckout() {
    if (!user) {
      setShowAuth(true);
      return;
    }
    if (!orderId || cart.length === 0) {
      notify("Your cart is empty.");
      return;
    }

    try {
      setLoading(true);
      const response = await authFetch(
        `${API_URL}/orders/checkout/${orderId}`,
        { method: "POST" }
      );

      if (!response.ok) {
        const text = await response.text();
        throw new Error(text || "Checkout failed");
      }

      const order = await response.json();
      setCart([]);
      setOrderId(null);
      localStorage.removeItem("activeOrderId");
      await loadOrders();
      setActivePage("orders");
      notify(`Order #${order.id} confirmed! Total ₹${order.totalPrice}.`);
    } catch (error) {
      console.error(error);
      notify(error.message || "Checkout failed.");
    } finally {
      setLoading(false);
    }
  }

  async function advanceOrderStatus(order) {
    const nextStatus = {
      PLACED: "CONFIRMED",
      CONFIRMED: "PREPARING",
      PREPARING: "OUT_FOR_DELIVERY",
      OUT_FOR_DELIVERY: "DELIVERED",
    }[order.status];

    if (!nextStatus) return;

    try {
      setLoading(true);
      const response = await authFetch(
        `${API_URL}/orders/${order.id}/status?status=${nextStatus}`,
        { method: "PUT" }
      );

      const text = await response.text();
      if (!response.ok) throw new Error(text || "Status update failed");

      await loadOrders();
      notify(`Order #${order.id} is now ${nextStatus.replaceAll("_", " ")}.`);
    } catch (error) {
      console.error(error);
      notify(error.message || "Unable to update order status.");
    } finally {
      setLoading(false);
    }
  }

  async function cancelOrder(order) {
    if (order.status !== "PLACED" && order.status !== "CONFIRMED") return;

    try {
      setLoading(true);
      const response = await authFetch(
        `${API_URL}/orders/${order.id}/status?status=CANCELLED`,
        { method: "PUT" }
      );
      const text = await response.text();
      if (!response.ok) throw new Error(text || "Unable to cancel order");
      await loadOrders();
      notify(`Order #${order.id} cancelled.`);
    } catch (error) {
      console.error(error);
      notify(error.message || "Unable to cancel order.");
    } finally {
      setLoading(false);
    }
  }

  function openRating(order, cakeId) {
    if (!order || order.status !== "DELIVERED") {
      notify("Open My Orders and select a delivered order to rate it.");
      setActivePage("orders");
      return;
    }
    const cake = cakes.find((c) => Number(c.id) === Number(cakeId));
    setRatingOrder(order);
    setRatingCake(cake || { id: cakeId, name: `Cake #${cakeId}` });
    setRatingValue(5);
    setReviewText("");
    setActivePage("orders");
    window.scrollTo({ top: 0, behavior: "smooth" });
  }

  async function submitRating() {
    if (!ratingOrder || !ratingCake) return;

    try {
      setLoading(true);
      const response = await authFetch(`${API_URL}/ratings`, {
        method: "POST",
        body: JSON.stringify({
          orderId: Number(ratingOrder.id),
          cakeId: Number(ratingCake.id),
          customerName: user.name,
          rating: Number(ratingValue),
          review: reviewText,
        }),
      });

      const text = await response.text();
      if (!response.ok) throw new Error(text || "Rating submission failed");

      await loadMyRatings();
      setRatingOrder(null);
      setRatingCake(null);
      setReviewText("");
      setRatingValue(5);
      notify("⭐ Your rating was submitted successfully.");
    } catch (error) {
      console.error(error);
      notify(error.message || "Unable to submit rating.");
    } finally {
      setLoading(false);
    }
  }

  async function handleRegister(name, email, password) {
    try {
      setLoading(true);
      const response = await fetch(`${API_URL}/users/register`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ name, email, password }),
      });

      const text = await response.text();
      if (!response.ok) throw new Error(text || "Registration failed");

      const newUser = JSON.parse(text);
      setUser(newUser);
      localStorage.setItem("user", JSON.stringify(newUser));

      if (newUser.token) localStorage.setItem("token", newUser.token);

      setShowAuth(false);
      await loadOrders();
      notify(`Welcome to Cake Delight, ${newUser.name}!`);
    } catch (error) {
      console.error(error);
      notify(error.message || "Registration failed.");
    } finally {
      setLoading(false);
    }
  }

  async function handleLogin(email, password) {
    try {
      setLoading(true);
      const response = await fetch(`${API_URL}/users/login`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ email, password }),
      });

      const text = await response.text();
      if (!response.ok) throw new Error(text || "Invalid email or password");

      const loggedInUser = JSON.parse(text);
      localStorage.setItem("token", loggedInUser.token);
      localStorage.setItem("user", JSON.stringify(loggedInUser));
      setUser(loggedInUser);
      setShowAuth(false);
      notify(`Welcome back, ${loggedInUser.name}!`);
    } catch (error) {
      console.error(error);
      notify(error.message || "Login failed.");
    } finally {
      setLoading(false);
    }
  }

  function handleLogout(showMessage = true) {
    localStorage.removeItem("token");
    localStorage.removeItem("user");
    localStorage.removeItem("activeOrderId");
    setUser(null);
    setCart([]);
    setOrders([]);
    setMyRatings([]);
    setOrderId(null);
    setRatingOrder(null);
    if (showMessage) notify("You have been logged out.");
  }

  const categories = useMemo(
    () => ["All", ...new Set(cakes.map((c) => c.category).filter(Boolean))],
    [cakes]
  );

  const filteredCakes = useMemo(() => {
    return cakes.filter((cake) => {
      const q = search.toLowerCase().trim();
      const matchesSearch =
        !q ||
        cake.name?.toLowerCase().includes(q) ||
        cake.description?.toLowerCase().includes(q);
      const matchesCategory =
        category === "All" || cake.category === category;
      return matchesSearch && matchesCategory;
    });
  }, [cakes, search, category]);

  const cartCount = cart.reduce((sum, item) => sum + Number(item.quantity || 0), 0);
  const cartTotal = cart.reduce(
    (sum, item) => sum + Number(item.price || 0) * Number(item.quantity || 0),
    0
  );

  const deliveredOrders = orders.filter((o) => o.status === "DELIVERED");
  const activeOrders = orders.filter(
    (o) => !["DELIVERED", "CANCELLED"].includes(o.status)
  );

  const stats = [
    ["🍰", `${cakes.length || "Fresh"}+`, "Cake varieties"],
    ["🚚", "Fast", "Delivery service"],
    ["⭐", "5/5", "Customer love"],
    ["🎉", "Every", "Celebration"],
  ];

  return (
    <div className="store-app">
      <div className="top-strip">Freshly baked happiness • Order today • Celebrate beautifully</div>

      <header className="store-header">
        <button className="brand" onClick={() => setActivePage("home")}>
          <span className="brand-mark">🍰</span>
          <span>
            <b>Cake Delight</b>
            <small>Sweet moments, beautifully baked</small>
          </span>
        </button>

        <nav className="desktop-nav">
          <button className={activePage === "home" ? "active" : ""} onClick={() => setActivePage("home")}>Home</button>
          <button className={activePage === "shop" ? "active" : ""} onClick={() => setActivePage("shop")}>Shop</button>
          {user && <button className={activePage === "orders" ? "active" : ""} onClick={() => setActivePage("orders")}>My Orders</button>}
          {user && <button className={activePage === "ratings" ? "active" : ""} onClick={() => setActivePage("ratings")}>My Reviews</button>}
        </nav>

        <div className="header-actions">
          <button className="cart-pill" onClick={() => setActivePage("cart")}>🛒 <span>{cartCount}</span></button>
          {user ? (
            <div className="profile-menu">
              <span className="avatar">{user.name?.charAt(0)?.toUpperCase() || "U"}</span>
              <span className="profile-name">{user.name}</span>
              <button className="ghost-btn" onClick={() => handleLogout()}>Logout</button>
            </div>
          ) : (
            <button className="primary-btn small" onClick={() => { setAuthMode("login"); setShowAuth(true); }}>Login</button>
          )}
        </div>
      </header>

      {message && <div className="toast">✓ {message}</div>}

      {activePage === "home" && (
        <>
          <section className="hero-store">
            <div className="hero-copy">
              <span className="eyebrow">HANDCRAFTED WITH LOVE</span>
              <h1>Make every celebration <em>extra sweet.</em></h1>
              <p>Discover beautifully crafted cakes made for birthdays, anniversaries, festivals and every little reason to celebrate.</p>
              <div className="hero-buttons">
                <button className="primary-btn" onClick={() => setActivePage("shop")}>Shop Cakes →</button>
                {!user && <button className="secondary-btn" onClick={() => { setAuthMode("register"); setShowAuth(true); }}>Create Account</button>}
              </div>
              <div className="hero-note">✓ Fresh ingredients &nbsp; ✓ Secure ordering &nbsp; ✓ Easy tracking</div>
            </div>
            <div className="hero-art">
              <div className="cake-glow"></div>
              <div className="cake-emoji">🎂</div>
              <div className="floating-card card-one">⭐ <b>4.9</b><small>Happy customers</small></div>
              <div className="floating-card card-two">🚚 <b>Fresh delivery</b><small>Track every order</small></div>
            </div>
          </section>

          <section className="stats-row">
            {stats.map(([icon, value, label]) => (
              <div className="stat-card" key={label}><span>{icon}</span><div><b>{value}</b><small>{label}</small></div></div>
            ))}
          </section>

          <section className="store-section">
            <div className="section-heading">
              <div><span className="eyebrow">OUR COLLECTION</span><h2>Made for every sweet moment</h2><p>Pick a favourite and let us handle the delicious part.</p></div>
              <button className="text-btn" onClick={() => setActivePage("shop")}>View all cakes →</button>
            </div>
            <CakeGrid cakes={filteredCakes.slice(0, 4)} onAdd={addToCart} onRate={openRating} user={user} />
          </section>

          <section className="feature-banner">
            <div><span className="eyebrow">WHY CAKE DELIGHT?</span><h2>A little sweetness goes a long way.</h2><p>From your first click to final delivery, your order is tracked through our cloud-native microservices.</p></div>
            <div className="feature-icons"><span>🧁</span><span>💖</span><span>🚚</span></div>
          </section>
        </>
      )}

      {activePage === "shop" && (
        <section className="store-section page-section">
          <div className="section-heading">
            <div><span className="eyebrow">SHOP</span><h2>Our Cake Collection</h2><p>Search, filter and choose your favourite.</p></div>
          </div>
          <div className="shop-toolbar">
            <div className="search-box">⌕<input value={search} onChange={(e) => setSearch(e.target.value)} placeholder="Search cakes, flavours..." /></div>
            <select value={category} onChange={(e) => setCategory(e.target.value)}>{categories.map((c) => <option key={c}>{c}</option>)}</select>
          </div>
          <CakeGrid cakes={filteredCakes} onAdd={addToCart} onRate={openRating} user={user} />
        </section>
      )}

      {activePage === "cart" && (
        <section className="store-section page-section narrow">
          <div className="section-heading"><div><span className="eyebrow">YOUR BASKET</span><h2>Ready to checkout?</h2><p>Review your cakes before placing the order.</p></div></div>
          {cart.length === 0 ? (
            <EmptyState icon="🛒" title="Your cart is empty" text="Add something delicious from our collection." action="Browse Cakes" onClick={() => setActivePage("shop")} />
          ) : (
            <div className="cart-layout">
              <div className="cart-items">
                {cart.map((item) => (
                  <div className="cart-row" key={item.basketItemId || item.id}>
                    <div className="mini-cake">🍰</div>
                    <div className="cart-info"><b>{item.name}</b><span>₹{item.price} each</span></div>
                    <div className="qty"><button onClick={() => decreaseQuantity(item)} disabled={loading}>−</button><b>{item.quantity}</b><button onClick={() => increaseQuantity(item)} disabled={loading}>+</button></div>
                    <b className="row-total">₹{item.price * item.quantity}</b>
                    <button className="remove-btn" onClick={() => removeFromCart(item)}>Remove</button>
                  </div>
                ))}
              </div>
              <div className="checkout-card">
                <h3>Order Summary</h3>
                <div><span>Items</span><b>{cartCount}</b></div>
                <div><span>Subtotal</span><b>₹{cartTotal}</b></div>
                <div><span>Delivery</span><b>FREE</b></div>
                <hr /><div className="grand"><span>Total</span><b>₹{cartTotal}</b></div>
                <button className="primary-btn full" onClick={handleCheckout} disabled={loading}>{loading ? "Processing..." : "Place Order →"}</button>
                <small>Secure checkout • Your order will appear in My Orders</small>
              </div>
            </div>
          )}
        </section>
      )}

      {activePage === "orders" && user && (
        <section className="store-section page-section">
          <div className="section-heading">
            <div><span className="eyebrow">ACCOUNT</span><h2>My Orders</h2><p>Track every order from kitchen to doorstep.</p></div>
            <button className="secondary-btn" onClick={loadOrders}>↻ Refresh</button>
          </div>
          {orders.length === 0 ? <EmptyState icon="📦" title="No orders yet" text="Your delicious journey starts with your first order." action="Shop Cakes" onClick={() => setActivePage("shop")} /> :
            <div className="orders-grid">
              {orders.slice().sort((a,b)=>Number(b.id)-Number(a.id)).map((order) => {
                const statusIndex = ["PLACED","CONFIRMED","PREPARING","OUT_FOR_DELIVERY","DELIVERED"].indexOf(order.status);
                const cake = cakes.find(c => Number(c.id) === Number(order.cakeId));
                return (
                  <article className="order-card" key={order.id}>
                    <div className="order-top"><div><span className="order-number">ORDER #{order.id}</span><h3>{cake?.name || `Cake #${order.cakeId}`}</h3></div><StatusBadge status={order.status}/></div>
                    <div className="order-meta"><span>🍰 Qty {order.quantity}</span><span>₹{order.totalPrice}</span></div>
                    <div className="progress">
                      {["PLACED","CONFIRMED","PREPARING","OUT_FOR_DELIVERY","DELIVERED"].map((s,i)=><div className={`progress-step ${i <= statusIndex ? "done":""}`} key={s}><span>{i < statusIndex ? "✓" : i+1}</span><small>{s.replaceAll("_"," ")}</small></div>)}
                    </div>
                    <div className="order-actions">
                      {!["DELIVERED","CANCELLED"].includes(order.status) && <button className="primary-btn small" onClick={() => advanceOrderStatus(order)} disabled={loading}>Move to {({PLACED:"Confirmed",CONFIRMED:"Preparing",PREPARING:"Out for Delivery",OUT_FOR_DELIVERY:"Delivered"})[order.status]}</button>}
                      {["PLACED","CONFIRMED"].includes(order.status) && <button className="danger-btn" onClick={() => cancelOrder(order)} disabled={loading}>Cancel</button>}
                      {order.status === "DELIVERED" && <button className="primary-btn small" onClick={() => openRating(order, order.cakeId)}>⭐ Rate this order</button>}
                      {order.status === "DELIVERED" && <span className="delivered-note">✓ Delivered successfully</span>}
                    </div>
                  </article>
                );
              })}
            </div>
          }
        </section>
      )}

      {activePage === "ratings" && user && (
        <section className="store-section page-section narrow">
          <div className="section-heading"><div><span className="eyebrow">YOUR FEEDBACK</span><h2>My Ratings & Reviews</h2><p>Your submitted feedback in one place.</p></div><button className="secondary-btn" onClick={loadMyRatings}>↻ Refresh</button></div>
          {myRatings.length === 0 ? (
            <EmptyState
              icon="⭐"
              title="No reviews yet"
              text="Rate a delivered order and your review will appear here."
              action="View Orders"
              onClick={() => setActivePage("orders")}
            />
          ) : (
            <div className="review-grid">
              {myRatings
                .slice()
                .reverse()
                .map((rating) => {
                  const cake = cakes.find(
                    (c) => Number(c.id) === Number(rating.cakeId)
                  );

                  const ratingNumber = Math.max(
                    1,
                    Math.min(5, Number(rating.rating) || 0)
                  );

                  return (
                    <article className="review-card" key={rating.id}>
                      <div className="review-head">
                        <div className="review-avatar">
                          {user?.name?.charAt(0)?.toUpperCase() || "U"}
                        </div>

                        <div className="review-user-info">
                          <b>{user?.name || "You"}</b>
                          <small>
                            Order #{rating.orderId} •{" "}
                            {cake?.name || `Cake #${rating.cakeId}`}
                          </small>
                        </div>

                        <div className="review-stars" aria-label={`${ratingNumber} out of 5 stars`}>
                          {"★".repeat(ratingNumber)}
                          <span className="review-rating-number">
                            {ratingNumber}/5
                          </span>
                        </div>
                      </div>

                      <div className="review-content">
                        <span className="review-label">YOUR REVIEW</span>
                        <p>
                          {rating.review?.trim()
                            ? `“${rating.review}”`
                            : "You submitted a rating without a written review."}
                        </p>
                      </div>

                      <div className="review-footer">
                        <span>🍰 {cake?.name || `Cake #${rating.cakeId}`}</span>
                        <span>✓ Verified purchase</span>
                      </div>
                    </article>
                  );
                })}
            </div>
          )}
        </section>
      )}

      {user && activePage === "home" && activeOrders.length > 0 && (
        <section className="store-section">
          <div className="section-heading"><div><span className="eyebrow">LIVE ORDERS</span><h2>Orders in progress</h2></div><button className="text-btn" onClick={() => setActivePage("orders")}>Track orders →</button></div>
          <div className="orders-mini">{activeOrders.slice(0,3).map(o => <div key={o.id} className="mini-order"><span>📦 <b>#{o.id}</b></span><StatusBadge status={o.status}/><span>₹{o.totalPrice}</span></div>)}</div>
        </section>
      )}

      {ratingOrder && ratingCake && (
        <div className="modal-backdrop" onClick={() => setRatingOrder(null)}>
          <div className="rating-modal" onClick={(e) => e.stopPropagation()}>
            <button className="modal-close" onClick={() => setRatingOrder(null)}>×</button>
            <span className="modal-icon">⭐</span>
            <span className="eyebrow">ORDER #{ratingOrder.id} • DELIVERED</span>
            <h2>How was your cake?</h2>
            <p>Tell us what you thought about <b>{ratingCake.name}</b>.</p>
            <div className="star-picker">{[1,2,3,4,5].map(n => <button key={n} className={n <= ratingValue ? "selected":""} onClick={() => setRatingValue(n)}>★</button>)}</div>
            <textarea value={reviewText} onChange={(e)=>setReviewText(e.target.value)} placeholder="Write a review (optional)..." rows={5}/>
            <button className="primary-btn full" onClick={submitRating} disabled={loading}>{loading ? "Submitting..." : "Submit Review"}</button>
          </div>
        </div>
      )}

      {showAuth && (
        <AuthModal mode={authMode} setMode={setAuthMode} onClose={() => setShowAuth(false)} onLogin={handleLogin} onRegister={handleRegister} loading={loading}/>
      )}

      <footer className="footer">
        <div><div className="brand footer-brand"><span className="brand-mark">🍰</span><span><b>Cake Delight</b><small>Sweet moments, beautifully baked</small></span></div><p>Fresh cakes, smooth ordering and happy celebrations.</p></div>
        <div><b>Explore</b><button onClick={()=>setActivePage("shop")}>Shop Cakes</button><button onClick={()=>setActivePage("orders")}>Track Orders</button><button onClick={()=>setActivePage("ratings")}>Reviews</button></div>
        <div><b>Store</b><span>Freshly baked</span><span>Secure checkout</span><span>Order tracking</span></div>
      </footer>
    </div>
  );
}

function CakeGrid({ cakes, onAdd, onRate, user }) {
  if (!cakes.length) {
    return (
      <EmptyState
        icon="🍰"
        title="No cakes found"
        text="Try a different search or category."
      />
    );
  }

  const getCakeImage = (cake) => {
    const imageMap = {
      "black-forest.jpg": "/images/black-forest.jpg",
      "chocolate-truffle.jpg": "/images/chocolate-truffle.jpeg",
      "chocolate-truffle.jpeg": "/images/chocolate-truffle.jpeg",
      "red-velvet.jpg": "/images/red-velvet.jpg",
      "strawberry.jpg": "/images/strawberry.jpeg",
      "strawberry.jpeg": "/images/strawberry.jpeg",
      "butterscotch.jpg": "/images/butterscotch.avif",
      "butterscotch.avif": "/images/butterscotch.avif",
      "vanilla.jpg": "/images/vanilla.jpg",
      "mango.jpg": "/images/mango.jpeg",
      "mango.jpeg": "/images/mango.jpeg",
      "ferrero-rocher.jpg": "/images/ferrero-rocher.jpeg",
      "ferrero-rocher.jpeg": "/images/ferrero-rocher.jpeg",
    };

    return imageMap[cake.imageReference] || "/images/black-forest.jpg";
  };

  return (
    <div className="cake-grid">
      {cakes.map((cake) => {
        const image = getCakeImage(cake);

        return (
          <article className="cake-card-new" key={cake.id}>

            {/* REAL CAKE IMAGE */}
            <div className="cake-visual">
              <img
                src={image}
                alt={cake.name}
                className="real-cake-image"
                onError={(e) => {
                  e.currentTarget.src = "/images/black-forest.jpg";
                }}
              />

              <div className="cake-tag">
                {cake.category || "Fresh Cake"}
              </div>
            </div>

            {/* CAKE DETAILS */}
            <div className="cake-body">

              <h3>{cake.name}</h3>

              <p>
                {cake.description ||
                  "Freshly baked with delicious ingredients."}
              </p>

              <div className="cake-rating">
                ★★★★★
                <small> Customer favourite</small>
              </div>

              <div className="cake-footer">

                <b>₹{cake.price}</b>

                <button
                  className="primary-btn small"
                  onClick={() => onAdd(cake)}
                  disabled={cake.available === false}
                >
                  {cake.available === false
                    ? "Out of Stock"
                    : "Add to Cart"}
                </button>

              </div>

              <button
                className="rate-link"
                onClick={() => onRate(null, cake.id)}
              >
                ⭐ Rate after delivery
              </button>

            </div>

          </article>
        );
      })}
    </div>
  );
}

function StatusBadge({ status }) {
  const label = status?.replaceAll("_"," ") || "UNKNOWN";
  return <span className={`status-badge status-${status?.toLowerCase()}`}>{status === "DELIVERED" ? "✓ " : "• "}{label}</span>;
}

function EmptyState({ icon, title, text, action, onClick }) {
  return <div className="empty-state"><span>{icon}</span><h3>{title}</h3><p>{text}</p>{action && <button className="primary-btn small" onClick={onClick}>{action}</button>}</div>;
}

function AuthModal({ mode, setMode, onClose, onLogin, onRegister, loading }) {
  const [name,setName]=useState("");
  const [email,setEmail]=useState("");
  const [password,setPassword]=useState("");

  function submit(e) {
    e.preventDefault();
    if (mode === "login") onLogin(email,password);
    else onRegister(name,email,password);
  }

  return <div className="modal-backdrop" onClick={onClose}><div className="auth-modal" onClick={e=>e.stopPropagation()}>
    <button className="modal-close" onClick={onClose}>×</button>
    <div className="auth-art">🍰</div>
    <span className="eyebrow">{mode === "login" ? "WELCOME BACK" : "JOIN CAKE DELIGHT"}</span>
    <h2>{mode === "login" ? "Good to see you again." : "Your sweetest account starts here."}</h2>
    <p>{mode === "login" ? "Login to track orders, manage your basket and leave reviews." : "Create an account to order your favourite cakes in seconds."}</p>
    <form onSubmit={submit} className="auth-form-new">
      {mode === "register" && <label>Full name<input value={name} onChange={e=>setName(e.target.value)} placeholder="Vikas Kumar" required/></label>}
      <label>Email<input type="email" value={email} onChange={e=>setEmail(e.target.value)} placeholder="you@example.com" required/></label>
      <label>Password<input type="password" value={password} onChange={e=>setPassword(e.target.value)} placeholder="••••••••" required/></label>
      <button className="primary-btn full" type="submit" disabled={loading}>{loading ? "Please wait..." : mode === "login" ? "Login to Cake Delight →" : "Create My Account →"}</button>
    </form>
    <div className="auth-switch">{mode === "login" ? "New here?" : "Already a member?"} <button onClick={()=>setMode(mode==="login"?"register":"login")}>{mode==="login"?"Create an account":"Login instead"}</button></div>
  </div></div>;
}

export default App;
