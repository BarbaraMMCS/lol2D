package lol.client.ai;

import java.util.*;
import lol.game.*;
import lol.game.action.*;

public class RandomAI extends AIBase {
  protected Random random;
  protected BattlefieldTraversal traversal;

  public RandomAI(Arena arena, Battlefield battlefield) {
    super(arena, battlefield);
    traversal = new BattlefieldTraversal(battlefield);
    random = new Random();
  }
  public Turn championSelect() {
    Turn turn = new Turn();
    switch(teamID) {
      case Nexus.BLUE: addChampions(4, "Warrior", turn);addChampions(4, "Archer", turn); break;
      case Nexus.RED: addChampions(4, "Archer", turn);addChampions(4, "Warrior", turn); break;
      default: throw new RuntimeException("Unknown team color.");
    }

    return turn;
  }

  private void addChampions(int nChampion, String championName, Turn turn) {
    for (int i = 0; i < nChampion; i++) {
      turn.registerAction(new ChampionSelect(teamID, championName));
    }
  }

  public Turn turn() {
    Turn turn = new Turn();
    tryAttackNexus(turn);
    tryAttackMonster(turn);
    tryMove(turn);
    return turn;
  }

  protected void tryAttackNexus(Turn turn) {
    arena.teamOf(teamID).forEachChampion((champion, id) ->
      traversal.visitAdjacent(champion.x(), champion.y(), champion.attackRange(), new TileVisitor(){
        public void visitNexus(Nexus nexus) {
          if(nexus.teamOfNexus() != teamID) {
            turn.registerAction(new Attack(teamID, id, nexus.x(), nexus.y()));
          }
        }
      }));
  }

  protected void tryMove(Turn turn) {
    Nexus ennemyNexus = battlefield.nexusOf((teamID + 1) % 2);
    arena.teamOf(teamID).forEachChampion((champion, id) -> {
      traversal.visitAdjacent(champion.x(), champion.y(), champion.walkSpeed(), new TileVisitor(){
        public void visitGrass(int x, int y) {
          if(distance(x, y, ennemyNexus.x(), ennemyNexus.y())<distance(champion.x(), champion.y(), ennemyNexus.x(), ennemyNexus.y())) {
            turn.registerAction(new Move(teamID, id, x, y));
            System.out.println("teamID " + teamID + " id " + id + " x " + x + " y " + y);
          }
        }
      });
      traversal.visitAdjacent(champion.x(), champion.y(), champion.walkSpeed(), new TileVisitor(){
        public void visitGrass(int x, int y) {
          if(manhattanDistance(x, y, ennemyNexus.x(), ennemyNexus.y())<manhattanDistance(champion.x(), champion.y(), ennemyNexus.x(), ennemyNexus.y())) {
            turn.registerAction(new Move(teamID, id, x, y));
            System.out.println("teamID " + teamID + " id " + id + " x " + x + " y " + y);
          }
        }
      });
    });
  }

  private int manhattanDistance(int x1, int y1, int x2, int y2){
    return Math.abs(x1 - x2) + Math.abs(y1 - y2);
  }

  protected int distance(int x1, int y1, int x2, int y2) {
        return Math.max(Math.abs(x1 - x2), Math.abs(y1 - y2));
    }

  protected void tryAttackMonster(Turn turn) {
    arena.teamOf(teamID).forEachChampion((champion, id) ->
      traversal.visitAdjacent(champion.x(), champion.y(), champion.attackRange(), new TileVisitor(){
        public void visitMonster(Monster monster) {
          turn.registerAction(new Attack(teamID, id, monster.x(), monster.y()));
        }
      }));
  }

  private void tryAttackTower(Turn turn) {
    arena.teamOf(teamID).forEachChampion((champion, id) ->
      traversal.visitAdjacent(champion.x(), champion.y(), champion.attackRange(), new TileVisitor(){
        public void visitTower(Tower tower) {
        if(tower.teamOfTower() != teamID) {
          turn.registerAction(new Attack(teamID, id, tower.x(), tower.y()));
        }
        }
      }));
  }
}
