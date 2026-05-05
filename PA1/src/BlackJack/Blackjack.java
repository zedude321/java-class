package BlackJack;

import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

public class Blackjack {
	static Deck deck;
	static int numberOfPlayers;
	static House house;
	static Computer[] bots;
	static Player player;

	static void dealCards() {
		player.drawCard(deck.dealCard());
		for (Computer b : bots) {
			b.drawCard(deck.dealCard());
		}
		house.drawCard(deck.dealCard());
	}

	static void printLines() {
		house.printLine();
		player.printLine();
		for (Computer b : bots) {
			b.printLine();
		}
	}

	static void playGame() {
		dealCards();
		dealCards();
		printLines();
		player.startTurn(deck);
		for (Computer b : bots) {
			b.startTurn(deck);
		}

		house.startTurn(deck);
		int houseHand = house.getCalculatedHand();
		Boolean dealerHas21 = house.has21();

		System.out.println("\n--- Game Results ---");

		house.printLine();
		if (dealerHas21) {
			player.printFinalLine();
		} else {
			player.printFinalLine(houseHand);
		}
		for (Computer b : bots) {
			if (dealerHas21) {
				b.printFinalLine();
			} else {
				b.printFinalLine(houseHand);
			}
		}
	}

	public static void main(String[] args) {
		int seed = Integer.parseInt(args[0]);
		numberOfPlayers = Integer.parseInt(args[1]);

		if (numberOfPlayers > 5 || numberOfPlayers < 1) {
			throw new IllegalStateException("Invalid number of players");
		}

		deck = new Deck();
		house = new House();
		bots = new Computer[numberOfPlayers - 1];
		player = new Player();

		for (int i = 0; i < numberOfPlayers - 1; i++) {
			bots[i] = new Computer(i + 2);
		}

		deck.shuffle(seed);

		playGame();
	}
}

class Card {
	private int value;
	private int suit;

	public Card() {
	}

	public Card(int theValue, int theSuit) {
		this.value = theValue;
		this.suit = theSuit;
	}

	public String returnDisplaySuit() {
		switch (this.suit) {
		case 0:
			return "c"; // Clubs - 0
		case 1:
			return "h"; // Hearts - 1
		case 2:
			return "d"; // Diamonds -2
		case 3:
			return "s"; // Spades - 3
		default:
			throw new IllegalStateException("Invalid suit");
		}
	}

	public String returnDisplayValue() {
		switch (this.value) {
		case 13:
			return "K";
		case 12:
			return "Q";
		case 11:
			return "J";
		case 1:
			return "A";
		default:
			return String.valueOf(this.value);
		}
	}

	public int returnCalculatedValue() { // 10 if >10 11 if ==1
		if (this.value >= 10) {
			return 10;
		} else if (this.value == 1) {
			return 11;
		} else {
			return this.value;
		}
	}

	public String displayBoth() {
		return this.returnDisplayValue() + this.returnDisplaySuit();
	}
}

class Deck {
	private Card[] deck = new Card[52];
	private int cardsUsed;

	public Deck() { // c, h, d, s || A->K
		int size = 0;
		for (int val = 0; val < 13; val++) {
			for (int suit = 0; suit < 4; suit++) {
				deck[size] = new Card(val + 1, suit);
				size++;
			}
		}
	}

	public void shuffle(int seed) {
		Random random = new Random(seed);
		for (int i = deck.length - 1; i > 0; i--) {
			int rand = (int) (random.nextInt(i + 1));
			Card temp = deck[i];
			deck[i] = deck[rand];
			deck[rand] = temp;
		}
		cardsUsed = 0;
	}

	public Card dealCard() {
		if (cardsUsed == deck.length)
			throw new IllegalStateException("No cards are left in the deck.");

		cardsUsed++;
		return deck[cardsUsed - 1];
	}
}

abstract class Hand {
	enum GameState {
		Win, Lose, Draw
	}

	protected ArrayList<Card> cards;
	protected String name;
	protected int calculatedHand;
	protected GameState state;

	public Hand(String name) {
		this.name = name;
		this.calculatedHand = 0;
		this.cards = new ArrayList<>();
	}

	protected Boolean isBust() {
		return this.calculatedHand > 21;
	}

	protected void calculateState(int dealerValue) { // if dealer doesn't have Ace 10
		if (this.isBust()) {
			state = GameState.Lose;
		} else if (dealerValue > 21) {
			state = GameState.Win;
		} else if (this.calculatedHand < dealerValue) {
			state = GameState.Lose;
		} else if (this.calculatedHand == dealerValue) {
			state = GameState.Draw;
		} else {
			state = GameState.Win;
		}
	}

	protected void calculateHand() { // can be optimized
		int currentHand = 0;
		int acesUsed = 0;

		for (Card c : cards) {
			if (c.returnCalculatedValue() == 11)
				acesUsed++;
			currentHand += c.returnCalculatedValue();
			if (currentHand > 21 && acesUsed > 0) {
				currentHand -= 10;
				acesUsed--;
			}
		}

		this.calculatedHand = currentHand;
	}

	protected String displayCards() {
		String text = "";

		for (int i = 0; i < cards.size(); i++) {
			text += cards.get(i).displayBoth();
			if (i < cards.size() - 1)
				text += ", ";
		}

		return text;
	}

	protected abstract Boolean willHit();

	protected void turn(Deck deck) {
		if (this.isBust()) {
			return;
		}
		if (this.willHit()) {
			System.out.println("Hit");
			this.drawCard(deck.dealCard());
			this.printLine();
			this.turn(deck);
		} else {
			System.out.println("Stand");
			this.printLine();
		}
	}

	public void printLine() {
		System.out.print(name + ": ");
		System.out.print(this.displayCards());
		System.out.print(" (" + this.calculatedHand + ")");
		if (this.isBust()) {
			System.out.print(" - Bust!");
		}
		System.out.println();
	}

	public void printFinalLine() { // If dealer has Ace 10
		System.out.print("[Lose] ");
		this.printLine();
	}

	public void printFinalLine(int dealerValue) { // If dealer doesn't have Ace 10
		this.calculateState(dealerValue);

		System.out.print("[" + this.state + "] ");
		if (this.state == GameState.Win)
			System.out.print(" ");
		this.printLine();
	}

	public int getCalculatedHand() {
		return this.calculatedHand;
	}

	public void drawCard(Card card) {
		cards.add(card);
		this.calculateHand();
	}

	public void startTurn(Deck deck) {
		System.out.println("\n--- " + this.name + " ---");
		this.printLine();
		this.turn(deck);
	}
}

class Computer extends Hand {
	public Computer(int number) {
		super("Player" + number);
	}

	public Boolean willHit() {
		if (this.calculatedHand < 14)
			return true;
		if (this.calculatedHand > 17)
			return false;
		Random random = new Random();
		int is_hit = (int) random.nextInt(2);
		return is_hit == 1;
	}
}

class Player extends Hand {
	public Player() {
		super("Player1");
	}

	protected void turn(Deck deck, Scanner s) {
		if (this.isBust())
			return;
		String action = s.nextLine().toLowerCase();

		if (action.equals("hit")) {
			this.drawCard(deck.dealCard());
			this.printLine();
			this.turn(deck, s);
		} else {
			this.printLine();
		}
	}

	public void startTurn(Deck deck) {
		Scanner s = new Scanner(System.in);
		System.out.println("\n--- " + this.name + " ---");
		this.printLine();
		this.turn(deck, s);
		s.close();
	}

	public Boolean willHit() {
		return false;
	}
}

class House extends Hand {
	private Boolean hideCard = true;

	public House() {
		super("House");
	}

	protected String displayCards() {
		String text = "";

		for (int i = 0; i < cards.size(); i++) {
			if (i == 0 && hideCard) {
				text += "HIDDEN";
			} else {
				text += cards.get(i).displayBoth();
			}
			if (i < cards.size() - 1)
				text += ", ";
		}

		return text;
	}

	public void printLine() {
		if (!hideCard) {
			super.printLine();
		} else {
			System.out.print(name + ": ");
			System.out.print(this.displayCards());
			hideCard = false;
			System.out.println();
		}
	}

	public Boolean has21() {
		if (this.calculatedHand == 21 && this.cards.size() == 2) {
			return true;
		} else {
			return false;
		}
	}

	public Boolean willHit() {
		return this.calculatedHand < 17;
	}
}
