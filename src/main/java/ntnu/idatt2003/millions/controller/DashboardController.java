package ntnu.idatt2003.millions.controller;

import java.math.BigDecimal;
import ntnu.idatt2003.millions.config.GameContext;
import ntnu.idatt2003.millions.model.Exchange;
import ntnu.idatt2003.millions.model.Player;
import ntnu.idatt2003.millions.model.Share;
import ntnu.idatt2003.millions.model.Stock;
import ntnu.idatt2003.millions.model.observer.GameEvent;
import ntnu.idatt2003.millions.model.observer.GameObserver;
import ntnu.idatt2003.millions.model.time.GameTime;
import ntnu.idatt2003.millions.service.ExchangeService;
import ntnu.idatt2003.millions.util.Format;
import ntnu.idatt2003.millions.view.DashboardView;
import ntnu.idatt2003.millions.view.ErrorDialog;
import ntnu.idatt2003.millions.view.PortfolioPanel;
import ntnu.idatt2003.millions.view.StockDetailView;
import ntnu.idatt2003.millions.view.TransactionsPanel;
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

  private enum RightPanel { NONE, PORTFOLIO, TRANSACTIONS }

  private final DashboardView view;
  private final Player player;
  private final ExchangeService exchangeService;
  private final AppController appController;
  private final GameContext context;

  private final MarketController marketController;
  private final StockDetailController stockDetailController;

  private final PortfolioPanel portfolioPanel;
  private final TransactionsPanel transactionsPanel;

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
      DashboardView view,
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

    StockDetailView stockDetailView = new StockDetailView();
    view.setCenterPanel(stockDetailView);
    this.stockDetailController = new StockDetailController(
        stockDetailView, exchangeService, player, context.settings());

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

  private void toggleRightPanel(RightPanel panel) {
    if (currentRightPanel == panel) {
      view.setRightPanel(null);
      currentRightPanel = RightPanel.NONE;
    } else {
      view.setRightPanel(panel == RightPanel.PORTFOLIO ? portfolioPanel : transactionsPanel);
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
  }
}
