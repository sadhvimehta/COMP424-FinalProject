# COMP424 Final AI Project: Pentagon Swap
This repo contains my code for the COMP424-Final Project: Pentagon Swap AI Agent with Alpha-Beta Pruning.

Summary:
This is a preliminary AI agent implemented using alpha-beta pruning with a depth of 2.
The algorithm first aims to populate the middle square of each quadrant if not already occupied.
Following this, it applies the minimax algorithm with alpha-beta pruning. Through testing, the depth used is 2.

The utility function involves finding the difference between the cost of the agent's move and the cost of its opponent's move. These individual costs are calculated by giving specific points to any 3, 4, and 5 pieces in a row THAT HAVE POTENTIAL TO BECOME 5 IN A ROW. This includes horizontal, vertical, and diagonal rows. The higher the number of pieces in  a row, the largest the number of assigned points.

For example, given the agent is player white:
- W W W E E B would be assigned points.
- W W E W B E would NOT be assigned points.

To run:
1. Clone repo.
2. Run the 'ServerGUI.java' file.
3. On GUI, click on 'Launch' and select 'Launch server' option.
4. From 'Launch' menu, select which opponents you wish to launch (AI Agent is called 'StudentPlayer').
5. Lose to my AI agent.
