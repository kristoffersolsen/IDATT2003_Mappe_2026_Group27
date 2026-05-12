package ntnu.idatt2003.millions.dashboard.controller;

import java.math.BigDecimal;
import ntnu.idatt2003.millions.app.config.GameContext;
import ntnu.idatt2003.millions.market.model.Exchange;
import ntnu.idatt2003.millions.player.model.Player;
import ntnu.idatt2003.millions.player.model.Share;
import ntnu.idatt2003.millions.market.model.Stock;
import ntnu.idatt2003.millions.shared.observer.GameEvent;
import ntnu.idatt2003.millions.shared.observer.GameObserver;
import ntnu.idatt2003.millions.order.model.LimitOrder;
import ntnu.idatt2003.millions.shared.time.GameTime;
import ntnu.idatt2003.millions.market.service.ExchangeService;
import ntnu.idatt2003.millions.shared.util.Format;
import ntnu.idatt2003.millions.dashboard.view.DashboardScreen;
import ntnu.idatt2003.millions.shared.view.ErrorDialog;
import ntnu.idatt2003.millions.order.view.OrdersPanel;
import ntnu.idatt2003.millions.player.view.PortfolioPanel;
import ntnu.idatt2003.millions.market.view.StockDetailView;
import ntnu.idatt2003.millions.transaction.view.TransactionsPanel;
import ntnu.idatt2003.millions.app.controller.AppController;
import ntnu.idatt2003.millions.market.controller.MarketController;
import ntnu.idatt2003.millions.market.controller.StockDetailController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controller for the main dashboard.
 *
 * <p>Holds an {@link ExchangeService} for operations and observes the
 * underlying {@link Exchange} model for state changes.
 * Delegates center-panel logic to {@link StockDetailController} and
 * side-panel logic to {@link MarketController}.
 */
public class DashboardController implements GameObserver {

  private static final Logger log = LoggerFactory.getLogger(DashboardController.class);

  private static final int SKIP_FIVE_HOURS = 5;

  private enum RightPanel { NONE, PORTFOLIO, TRANSACTIONS, ORDERS }

  private final DashboardScreen view;
  private final Player player;
  private final ExchangeService exchangeService;
  private final AppController appController;
  private final GameContext context;

  private final MarketController marketController;
  private final StockDetailController stockDetailController;

  private final PortfolioPanel portfolioPanel;
  private final TransactionsPanel transactionsPanel;
  private final OrdersPanel ordersPanel;

  private RightPanel currentRightPanel = RightPanel.NONE;

  /**
   * Creates the controller, registers as observer, and performs initial refresh.
   *
   * @param view            the dashboard view
   * @param player          the active player
   * @param exchangeService the active exchange service
   * @param appController   the app-level controller
   * @param context         the game context for this session
   */
  public DashboardController(
      DashboardScreen view,
      Player player,
      ExchangeService exchangeService,
      AppController appController,
      GameContext context) {
    this.view = view;
    this.player = player;
    this.exchangeService = exchangeService;
    this.appController = appController;
    this.context = context;

    this.portfolioPanel = new PortfolioPanel(this::onSellShare);
    this.transactionsPanel = new TransactionsPanel();
    this.ordersPanel = new OrdersPanel(this::onCancelOrder);

    StockDetailView stockDetailView = new StockDetailView();
    view.setCenterPanel(stockDetailView);
    this.stockDetailController = new StockDetailController(
        stockDetailView, exchangeService, player, context);

    exchangeService.getExchange().addObserver(this);
    player.addObserver(this);

    this.marketController = new MarketController(
        view.getMarketView(),
        exchangeService,
        this::onStockSelected
    );

    wireSkipButtons();
    wireEndGameButton();
    wirePortfolioButton();
    wireTransactionsButton();
    wireOrdersButton();

    exchangeService.getExchange().getStocks().stream()
        .findFirst()
        .ifPresent(this::onStockSelected);

    refresh();
  }

  @Override
  public void onEvent(Object source, GameEvent event) {
    switch (event) {
      case SKIP_COMPLETED -> {
        marketController.refresh();
        stockDetailController.refresh();
        refresh();
      }
      case STOCK_PURCHASED, STOCK_SOLD, BALANCE_CHANGED, PORTFOLIO_CHANGED -> {
        stockDetailController.refresh();
        refresh();
      }
      case LIMIT_ORDER_PLACED, LIMIT_ORDER_CANCELLED -> refresh();
      case LIMIT_ORDER_EXECUTED -> {
        stockDetailController.refresh();
        refresh();
      }
      default -> {
      }
    }
  }

  /**
   * Unregisters this controller from all observable sources.
   *
   * <p>Called by {@link AppController} before swapping to a different
   * screen so observer lists don't accumulate stale references.
   */
  public void dispose() {
    exchangeService.getExchange().removeObserver(this);
    player.removeObserver(this);
  }

  private void onStockSelected(Stock stock) {
    stockDetailController.showStock(stock);
  }

  /**
   * Forwards a partial or full sell from the portfolio modal to the service.
   *
   * @param share          the position being sold
   * @param quantityToSell the quantity chosen in the modal
   */
  private void onSellShare(Share share, BigDecimal quantityToSell) {
    try {
      exchangeService.sell(share.stock().getSymbol(), quantityToSell, player);
    } catch (IllegalArgumentException e) {
      log.error("Sell failed", e);
      ErrorDialog.show("Sell failed", e.getMessage());
    }
  }

  private void onCancelOrder(LimitOrder order) {
    if (context.orderService() == null) {
      return;
    }
    try {
      context.orderService().cancelLimitOrder(player, order);
    } catch (IllegalArgumentException e) {
      log.error("Cancel order failed", e);
      ErrorDialog.show("Cancel failed", e.getMessage());
    }
  }

  private void wireSkipButtons() {
    int hoursPerDay = context.settings().hoursPerDay();
    int hoursPerWeek = hoursPerDay * context.settings().daysPerWeek();
    view.getSkip1hButton().setOnAction(e -> context.gameClock().advanceBy(1));
    view.getSkip5hButton().setOnAction(e -> context.gameClock().advanceBy(SKIP_FIVE_HOURS));
    view.getSkip1dButton().setOnAction(e -> context.gameClock().advanceBy(hoursPerDay));
    view.getSkip1wButton().setOnAction(e -> context.gameClock().advanceBy(hoursPerWeek));
  }

  private void wireEndGameButton() {
    view.getEndGameButton().setOnAction(
        e -> appController.showEndScreen(player, exchangeService, context));
  }

  private void wirePortfolioButton() {
    view.getPortfolioButton().setOnAction(e -> toggleRightPanel(RightPanel.PORTFOLIO));
  }

  private void wireTransactionsButton() {
    view.getTransactionsButton().setOnAction(e -> toggleRightPanel(RightPanel.TRANSACTIONS));
  }

  private void wireOrdersButton() {
    view.getOrdersButton().setOnAction(e -> toggleRightPanel(RightPanel.ORDERS));
  }

  private void toggleRightPanel(RightPanel panel) {
    if (currentRightPanel == panel) {
      view.setRightPanel(null);
      currentRightPanel = RightPanel.NONE;
    } else {
      view.setRightPanel(switch (panel) {
        case PORTFOLIO -> portfolioPanel;
        case TRANSACTIONS -> transactionsPanel;
        case ORDERS -> ordersPanel;
        default -> null;
      });
      currentRightPanel = panel;
    }
  }

  private void refresh() {
    GameTime currentTime = context.gameClock().currentTime();
    view.setTimeLabel(currentTime.format());
    view.updatePlayerInfo(
        player.getName(),
        player.getStatus(currentTime).getStatus(),
        Format.formatMoney(player.getNetWorth()),
        Format.formatMoney(player.getMoney()),
        Format.formatMoney(player.getPortfolio().getNetWorth()));

    portfolioPanel.setShares(player.getPortfolio().getShares());
    transactionsPanel.setTransactions(
        player.getTransactionArchive().getTransactions());

    if (context.orderService() != null) {
      ordersPanel.setOrders(context.orderService().getPendingOrders(player));
    }
  }
}
