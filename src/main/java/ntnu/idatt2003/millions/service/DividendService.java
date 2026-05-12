package ntnu.idatt2003.millions.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import ntnu.idatt2003.millions.model.Exchange;
import ntnu.idatt2003.millions.model.Player;
import ntnu.idatt2003.millions.model.Share;
import ntnu.idatt2003.millions.model.Stock;
import ntnu.idatt2003.millions.model.observer.GameEvent;
import ntnu.idatt2003.millions.model.transaction.Dividend;
import ntnu.idatt2003.millions.model.transaction.TransactionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Pays periodic dividends to shareholders on a configurable per-stock schedule.
 *
 * <p>On each simulated tick, stocks whose {@code dividendIntervalHours}
 * divides the current tick count evenly pay their {@code dividendPerShare}
 * amount to every registered player who holds that stock.
 *
 * <p>Payments are collected before any events are fired to prevent
 * re-entrant observer calls during the loop.
 */
public class DividendService {

  private static final Logger log = LoggerFactory.getLogger(DividendService.class);

  private final Exchange exchange;
  private final List<Player> players = new ArrayList<>();

  /**
   * Constructs the service for the given exchange.
   *
   * @param exchange the exchange whose stocks are checked for dividend eligibility
   */
  public DividendService(Exchange exchange) {
    this.exchange = exchange;
  }

  /**
   * Registers a player so they receive dividend payments.
   *
   * @param player the player to register
   */
  public void registerPlayer(Player player) {
    players.add(player);
  }

  /**
   * Pays dividends to all registered players for every eligible stock.
   *
   * <p>A stock is eligible when {@link Stock#paysDividend()} is true and
   * {@code tick % dividendIntervalHours == 0} (and {@code tick > 0}).
   * Payments for all eligible (player, stock) pairs are collected first,
   * then applied and notified, to avoid observer re-entrancy during iteration.
   *
   * <p>Called from {@link ExchangeService#tick()} after order evaluation.
   *
   * @param tick the current simulation tick
   */
  public void payDividends(long tick) {
    if (tick == 0) {
      return;
    }

    List<Player> recipients = new ArrayList<>();
    List<Dividend> payments = new ArrayList<>();

    for (Stock stock : exchange.getStocks()) {
      if (!stock.paysDividend()) {
        continue;
      }
      if (tick % stock.getDividendIntervalHours() != 0) {
        continue;
      }

      for (Player player : players) {
        Optional<Share> heldOpt = player.getPortfolio().getShareBySymbol(stock.getSymbol());
        if (heldOpt.isEmpty()) {
          continue;
        }
        Share held = heldOpt.get();
        Share dividendShare = new Share(stock, held.quantity(), stock.getDividendPerShare());
        recipients.add(player);
        payments.add(TransactionFactory.createDividend(dividendShare, tick));
      }
    }

    for (int i = 0; i < recipients.size(); i++) {
      Player player = recipients.get(i);
      Dividend tx = payments.get(i);
      player.addMoney(tx.getCalculator().calculateTotal());
      player.getTransactionArchive().add(tx);
      tx.markCommitted();
      log.info("Dividend paid: {} {} x ${} = ${}",
          tx.getShare().stock().getSymbol(),
          tx.getShare().quantity(),
          tx.getDividendPerShare(),
          tx.getTotalPaid());
      exchange.notifyObservers(GameEvent.DIVIDEND_PAID);
    }
  }
}
