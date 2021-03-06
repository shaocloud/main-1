package seedu.gtd.logic;

import com.google.common.eventbus.Subscribe;

import seedu.gtd.commons.core.EventsCenter;
import seedu.gtd.commons.events.model.AddressBookChangedEvent;
import seedu.gtd.commons.events.ui.JumpToListRequestEvent;
import seedu.gtd.commons.events.ui.ShowHelpRequestEvent;
import seedu.gtd.logic.Logic;
import seedu.gtd.logic.LogicManager;
import seedu.gtd.logic.commands.*;
import seedu.gtd.logic.parser.DateNaturalLanguageProcessor;
import seedu.gtd.logic.parser.NaturalLanguageProcessor;
import seedu.gtd.model.AddressBook;
import seedu.gtd.model.Model;
import seedu.gtd.model.ModelManager;
import seedu.gtd.model.ReadOnlyAddressBook;
import seedu.gtd.model.task.*;
import seedu.gtd.model.tag.Tag;
import seedu.gtd.model.tag.UniqueTagList;
import seedu.gtd.storage.StorageManager;
import seedu.gtd.testutil.TaskBuilder;
import seedu.gtd.testutil.TestTask;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static seedu.gtd.commons.core.Messages.*;

public class LogicManagerTest {
	
	//@@author addressbook-level4

    /**
     * See https://github.com/junit-team/junit4/wiki/rules#temporaryfolder-rule
     */
    @Rule
    public TemporaryFolder saveFolder = new TemporaryFolder();

    private Model model;
    private Logic logic;

    //These are for checking the correctness of the events raised
    private ReadOnlyAddressBook latestSavedAddressBook;
    private boolean helpShown;
    private int targetedJumpIndex;

    @Subscribe
    private void handleLocalModelChangedEvent(AddressBookChangedEvent abce) {
        latestSavedAddressBook = new AddressBook(abce.data);
    }

    @Subscribe
    private void handleShowHelpRequestEvent(ShowHelpRequestEvent she) {
        helpShown = true;
    }

    @Subscribe
    private void handleJumpToListRequestEvent(JumpToListRequestEvent je) {
        targetedJumpIndex = je.targetIndex;
    }

    @Before
    public void setup() {
        model = new ModelManager();
        String tempAddressBookFile = saveFolder.getRoot().getPath() + "TempAddressBook.xml";
        String tempPreferencesFile = saveFolder.getRoot().getPath() + "TempPreferences.json";
        logic = new LogicManager(model, new StorageManager(tempAddressBookFile, tempPreferencesFile));
        EventsCenter.getInstance().registerHandler(this);

        latestSavedAddressBook = new AddressBook(model.getAddressBook()); // last saved assumed to be up to date before.
        helpShown = false;
        targetedJumpIndex = -1; // non yet
    }

    @After
    public void teardown() {
        EventsCenter.clearSubscribers();
    }

    @Test
    public void execute_invalid() throws Exception {
        String invalidCommand = "       ";
        assertCommandBehavior(invalidCommand,
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, HelpCommand.MESSAGE_USAGE));
    }

    /**
     * Executes the command and confirms that the result message is correct.
     * Both the 'address book' and the 'last shown list' are expected to be empty.
     * @see #assertCommandBehavior(String, String, ReadOnlyAddressBook, List)
     */
    private void assertCommandBehavior(String inputCommand, String expectedMessage) throws Exception {
        assertCommandBehavior(inputCommand, expectedMessage, new AddressBook(), Collections.emptyList());
    }

    /**
     * Executes the command and confirms that the result message is correct and
     * also confirms that the following three parts of the LogicManager object's state are as expected:<br>
     *      - the internal address book data are same as those in the {@code expectedAddressBook} <br>
     *      - the backing list shown by UI matches the {@code shownList} <br>
     *      - {@code expectedAddressBook} was saved to the storage file. <br>
     */
    private void assertCommandBehavior(String inputCommand, String expectedMessage,
                                       ReadOnlyAddressBook expectedAddressBook,
                                       List<? extends ReadOnlyTask> expectedShownList) throws Exception {

        //Execute the command
        CommandResult result = logic.execute(inputCommand);
        System.out.println(inputCommand);

        //Confirm the ui display elements should contain the right data
        System.out.println(result.feedbackToUser);
        System.out.println(expectedMessage);
        assertEquals(expectedMessage, result.feedbackToUser);
        System.out.println("correct message");
        assertEquals(expectedShownList, model.getFilteredTaskList());
        System.out.println("correct data in UI");

        //Confirm the state of data (saved and in-memory) is as expected
        assertEquals(expectedAddressBook, model.getAddressBook());
        assertEquals(expectedAddressBook, latestSavedAddressBook);
    }


    @Test
    public void execute_unknownCommandWord() throws Exception {
        String unknownCommand = "uicfhmowqewca";
        assertCommandBehavior(unknownCommand, MESSAGE_UNKNOWN_COMMAND);
    }
    
    //@@author A0146130W-reusedToTest
    @Test
    public void execute_help() throws Exception {
        assertCommandBehavior("help", HelpCommand.MESSAGE_USAGE+"/n"+HelpCommand.SHOWING_HELP_MESSAGE);
        assertTrue(helpShown);
        assertCommandBehavior("help add", AddCommand.MESSAGE_USAGE);
        assertCommandBehavior("help select", SelectCommand.MESSAGE_USAGE);
        assertCommandBehavior("help delete", DeleteCommand.MESSAGE_USAGE);
        assertCommandBehavior("help clear", ClearCommand.MESSAGE_USAGE);
        assertCommandBehavior("help find", FindCommand.MESSAGE_USAGE);
        assertCommandBehavior("help list", ListCommand.MESSAGE_USAGE);
        assertCommandBehavior("help exit", ExitCommand.MESSAGE_USAGE);
    }

    //@@author addressbook-level4
    @Test
    public void execute_exit() throws Exception {
        assertCommandBehavior("exit", ExitCommand.MESSAGE_EXIT_ACKNOWLEDGEMENT);
    }

    @Test
    public void execute_clear() throws Exception {
        TestDataHelper helper = new TestDataHelper();
        model.addTask(helper.generateTask(1));
        model.addTask(helper.generateTask(2));
        model.addTask(helper.generateTask(3));

        assertCommandBehavior("clear", ClearCommand.MESSAGE_SUCCESS, new AddressBook(), Collections.emptyList());
    }
    
    @Test
    public void execute_add_invalidArgsFormat() throws Exception {
    	String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddCommand.MESSAGE_USAGE);
    		assertCommandBehavior(
    				"add ", expectedMessage);
    }
    

    @Test
    public void execute_add_invalidTaskData() throws Exception {
        assertCommandBehavior(
                "add []\\[;] d/12345 a/valid, address p/1", Name.MESSAGE_NAME_CONSTRAINTS);
        assertCommandBehavior(
                "add Valid Name d/not_numbers a/valid, address p/2", DueDate.MESSAGE_DUEDATE_CONSTRAINTS);
        assertCommandBehavior(
                "add Valid Name d/12345 a/valid, address p/not_priority_numbers", Priority.MESSAGE_PRIORITY_CONSTRAINTS);
        assertCommandBehavior(
                "add Valid Name d/12345 a/valid, address p/5 t/invalid_-[.tag", Tag.MESSAGE_TAG_CONSTRAINTS);

    }

    @Test
    public void execute_add_successful() throws Exception {
        // setup expectations
        TestDataHelper helper = new TestDataHelper();
        Task toBeAdded = helper.adam();
        Task dateChanged = helper.adamChanged();
        AddressBook expectedAB = new AddressBook();
        expectedAB.addTask(dateChanged);

        // execute command and verify result
        assertCommandBehavior(helper.generateAddCommand(toBeAdded),
                String.format(AddCommand.MESSAGE_SUCCESS, dateChanged),
                expectedAB,
                expectedAB.getTaskList());

    }
    
    @Test
    public void execute_add_optional_successful() throws Exception {
    	
        // setup expectations
    	TestDataHelper helper = new TestDataHelper();
    	Task intendedResult = helper.optionalAddressDateChanged();
    	AddressBook expectedAB = new AddressBook();
    	expectedAB.addTask(intendedResult);
    	String optionalAddressCmd = "add clean room d/noon p/3 t/tag1";
    	
        assertCommandBehavior(optionalAddressCmd,
                String.format(AddCommand.MESSAGE_SUCCESS, intendedResult),
                expectedAB,
                expectedAB.getTaskList());
    }

    @Test
    public void execute_addDuplicate_notAllowed() throws Exception {
        // setup expectations
        TestDataHelper helper = new TestDataHelper();
        Task toBeAdded = helper.adam();
        Task changedDate = helper.adamChanged();
        AddressBook expectedAB = new AddressBook();
        expectedAB.addTask(changedDate);

        // setup starting state
        model.addTask(changedDate); // task already in internal address book

        // execute command and verify result
        assertCommandBehavior(
                helper.generateAddCommand(toBeAdded),
                AddCommand.MESSAGE_DUPLICATE_TASK,
                expectedAB,
                expectedAB.getTaskList());

    }
    
    /*
    @Test
    public void execute_edit_successful() throws Exception {
        // setup expectations
        TestDataHelper helper = new TestDataHelper();
        Task toBeEdited = helper.adam();
        AddressBook expectedAB = helper.generateAddressBook(2);
        expectedAB.editTask(1, toBeEdited);

        // execute command and verify result
        assertCommandBehavior(helper.generateEditCommand(toBeEdited),
                String.format(EditCommand.MESSAGE_EDIT_TASK_SUCCESS, toBeEdited),
                expectedAB,
                expectedAB.getTaskList());
    }
    */


    @Test
    public void execute_list_showsAllTasks() throws Exception {
        // prepare expectations
        TestDataHelper helper = new TestDataHelper();
        AddressBook expectedAB = helper.generateAddressBook(2);
        List<? extends ReadOnlyTask> expectedList = expectedAB.getTaskList();

        // prepare address book state
        helper.addToModel(model, 2);

        assertCommandBehavior("list",
                ListCommand.MESSAGE_SUCCESS,
                expectedAB,
                expectedList);
    }


    /**
     * Confirms the 'invalid argument index number behaviour' for the given command
     * targeting a single task in the shown list, using visible index.
     * @param commandWord to test assuming it targets a single task in the last shown list based on visible index.
     */
    private void assertIncorrectIndexFormatBehaviorForCommand(String commandWord, String expectedMessage) throws Exception {
        assertCommandBehavior(commandWord , expectedMessage); //index missing
        assertCommandBehavior(commandWord + " +1", expectedMessage); //index should be unsigned
        assertCommandBehavior(commandWord + " -1", expectedMessage); //index should be unsigned
        assertCommandBehavior(commandWord + " 0", expectedMessage); //index cannot be 0
        assertCommandBehavior(commandWord + " not_a_number", expectedMessage);
    }

    /**
     * Confirms the 'invalid argument index number behaviour' for the given command
     * targeting a single task in the shown list, using visible index.
     * @param commandWord to test assuming it targets a single task in the last shown list based on visible index.
     */
    private void assertIndexNotFoundBehaviorForCommand(String commandWord) throws Exception {
        String expectedMessage = MESSAGE_INVALID_TASK_DISPLAYED_INDEX;
        TestDataHelper helper = new TestDataHelper();
        List<Task> taskList = helper.generateTaskList(2);

        // set AB state to 2 tasks
        model.clearTaskList();
        for (Task p : taskList) {
            model.addTask(p);
        }

        assertCommandBehavior(commandWord + " 3", expectedMessage, model.getAddressBook(), taskList);
    }

    @Test
    public void execute_selectInvalidArgsFormat_errorMessageShown() throws Exception {
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, SelectCommand.MESSAGE_USAGE);
        assertIncorrectIndexFormatBehaviorForCommand("select", expectedMessage);
    }

    @Test
    public void execute_selectIndexNotFound_errorMessageShown() throws Exception {
        assertIndexNotFoundBehaviorForCommand("select");
    }

    @Test
    public void execute_select_jumpsToCorrectTask() throws Exception {
        TestDataHelper helper = new TestDataHelper();
        List<Task> threeTasks = helper.generateTaskList(3);

        AddressBook expectedAB = helper.generateAddressBook(threeTasks);
        helper.addToModel(model, threeTasks);

        assertCommandBehavior("select 2",
                String.format(SelectCommand.MESSAGE_SELECT_TASK_SUCCESS, 2),
                expectedAB,
                expectedAB.getTaskList());
        assertEquals(1, targetedJumpIndex);
        assertEquals(model.getFilteredTaskList().get(1), threeTasks.get(1));
    }


    @Test
    public void execute_deleteInvalidArgsFormat_errorMessageShown() throws Exception {
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, DeleteCommand.MESSAGE_USAGE);
        assertIncorrectIndexFormatBehaviorForCommand("delete", expectedMessage);
    }

    @Test
    public void execute_deleteIndexNotFound_errorMessageShown() throws Exception {
        assertIndexNotFoundBehaviorForCommand("delete");
    }

    @Test
    public void execute_delete_removesCorrectTask() throws Exception {
        TestDataHelper helper = new TestDataHelper();
        List<Task> threeTasks = helper.generateTaskList(3);

        AddressBook expectedAB = helper.generateAddressBook(threeTasks);
        expectedAB.removeTask(threeTasks.get(1));
        helper.addToModel(model, threeTasks);

        assertCommandBehavior("delete 2",
                String.format(DeleteCommand.MESSAGE_DELETE_TASK_SUCCESS, threeTasks.get(1)),
                expectedAB,
                expectedAB.getTaskList());
    }


    @Test
    public void execute_find_invalidArgsFormat() throws Exception {
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindCommand.MESSAGE_USAGE);
        assertCommandBehavior("find ", expectedMessage);
    }

    @Test
    public void execute_find_onlyMatchesFullWordsInNames() throws Exception {
        TestDataHelper helper = new TestDataHelper();
        Task pTarget1 = helper.generateTaskWithName("bla bla KEY bla");
        Task pTarget2 = helper.generateTaskWithName("bla KEY bla bceofeia");
        Task p1 = helper.generateTaskWithName("KE Y");
        Task p2 = helper.generateTaskWithName("KEYKEYKEY sduauo");

        List<Task> fourTasks = helper.generateTaskList(p1, pTarget1, p2, pTarget2);
        AddressBook expectedAB = helper.generateAddressBook(fourTasks);
        List<Task> expectedList = helper.generateTaskList(pTarget1, pTarget2);
        helper.addToModel(model, fourTasks);

        assertCommandBehavior("find KEY",
                Command.getMessageForTaskListShownSummary(expectedList.size()),
                expectedAB,
                expectedList);
    }

    @Test
    public void execute_find_isNotCaseSensitive() throws Exception {
        TestDataHelper helper = new TestDataHelper();
        Task p1 = helper.generateTaskWithName("bla bla KEY bla");
        Task p2 = helper.generateTaskWithName("bla KEY bla bceofeia");
        Task p3 = helper.generateTaskWithName("key key");
        Task p4 = helper.generateTaskWithName("KEy sduauo");

        List<Task> fourTasks = helper.generateTaskList(p3, p1, p4, p2);
        AddressBook expectedAB = helper.generateAddressBook(fourTasks);
        List<Task> expectedList = fourTasks;
        helper.addToModel(model, fourTasks);

        assertCommandBehavior("find KEY",
                Command.getMessageForTaskListShownSummary(expectedList.size()),
                expectedAB,
                expectedList);
    }

    @Test
    public void execute_find_matchesIfAnyKeywordPresent() throws Exception {
        TestDataHelper helper = new TestDataHelper();
        Task pTarget1 = helper.generateTaskWithName("bla bla KEY bla");
        Task pTarget2 = helper.generateTaskWithName("bla rAnDoM bla bceofeia");
        Task pTarget3 = helper.generateTaskWithName("key key");
        Task p1 = helper.generateTaskWithName("sduauo");

        List<Task> fourTasks = helper.generateTaskList(pTarget1, p1, pTarget2, pTarget3);
        AddressBook expectedAB = helper.generateAddressBook(fourTasks);
        List<Task> expectedList = helper.generateTaskList(pTarget1, pTarget2, pTarget3);
        helper.addToModel(model, fourTasks);
        
        String keywords = "key rAnDoM";
        TestFindHelper findhelper = new TestFindHelper();
    	
        assertCommandBehavior("find " + keywords,
        		String.format(findhelper.generateCorrectResultIfExactPhraseNotFound(keywords, expectedList.size())),
                expectedAB,
                expectedList);
    }
    
    class TestFindHelper{
    	
    	String generateCorrectResultIfExactPhraseNotFound(String keywords, int expectedListSize) {	
            String task_tasks = (expectedListSize == 1) ? "task" : "tasks";
        	String MESSAGE_IF_EXACT_PHRASE_NOT_FOUND = "The exact phrase '" + keywords + "' was not found. Listing " + expectedListSize + " " + task_tasks + " containing the keywords entered instead.";
        	return MESSAGE_IF_EXACT_PHRASE_NOT_FOUND;
    	}
    }


    /**
     * A utility class to generate test data.
     */
    class TestDataHelper{

        Task adam() throws Exception {
            Name name = new Name("Pick up laundry");
            DueDate privateDueDate = new DueDate("noon");
            Address address = new Address("111, alpha street");
            Priority privatePriority = new Priority("1");
            Tag tag1 = new Tag("tag1");
            UniqueTagList tags = new UniqueTagList(tag1);
            return new Task(name, privateDueDate, address, privatePriority, tags);
        }
        Task adamChanged() throws Exception {
        	NaturalLanguageProcessor nlpTest = new DateNaturalLanguageProcessor();
        	String formattedDate = nlpTest.formatString("noon");
        	Name name = new Name("Pick up laundry");
            DueDate privateDueDate = new DueDate(formattedDate);
            Address address = new Address("111, alpha street");
            Priority privatePriority = new Priority("1");
            Tag tag1 = new Tag("tag1");
            UniqueTagList tags = new UniqueTagList(tag1);
            return new Task(name, privateDueDate, address, privatePriority, tags);
        }
        
        
        Task optionalAddressDateChanged() throws Exception {
        	NaturalLanguageProcessor nlpTest = new DateNaturalLanguageProcessor();
        	String formattedDate = nlpTest.formatString("noon");
        	Name name = new Name("clean room");
            DueDate privateDueDate = new DueDate(formattedDate);
            Address address = new Address("none");
            Priority privatePriority = new Priority("3");
            Tag tag1 = new Tag("tag1");
            UniqueTagList tags = new UniqueTagList(tag1);
            return new Task(name, privateDueDate, address, privatePriority, tags);
        }

        /**
         * Generates a valid task using the given seed.
         * Running this function with the same parameter values guarantees the returned task will have the same state.
         * Each unique seed will generate a unique Task object.
         *
         * @param seed used to generate the task data field values
         */
        Task generateTask(int seed) throws Exception {
            return new Task(
                    new Name("Task " + seed),
                    new DueDate("" + Math.abs(seed)),
                    new Address(seed + ", -address"),
                    new Priority("1 " + seed),
                    new UniqueTagList(new Tag("tag" + Math.abs(seed)), new Tag("tag" + Math.abs(seed + 1)))
            );
        }

        /** Generates the correct add command based on the task given */
        String generateAddCommand(Task p) {
            StringBuffer cmd = new StringBuffer();

            cmd.append("add ");

            cmd.append(p.getName().toString());
            cmd.append(" d/").append(p.getDueDate());
            cmd.append(" a/").append(p.getAddress());
            cmd.append(" p/").append(p.getPriority());

            UniqueTagList tags = p.getTags();
            for(Tag t: tags){
                cmd.append(" t/").append(t.tagName);
            }

            return cmd.toString();
        }
        
        /** Generates the correct add command based on the task given */
        String generateEditCommand(Task p) {
            StringBuffer cmd = new StringBuffer();

            cmd.append("edit ");
            cmd.append(" d/").append(p.getDueDate());

            return cmd.toString();
        }

        /**
         * Generates an AddressBook with auto-generated tasks.
         */
        AddressBook generateAddressBook(int numGenerated) throws Exception{
            AddressBook addressBook = new AddressBook();
            addToAddressBook(addressBook, numGenerated);
            return addressBook;
        }

        /**
         * Generates an AddressBook based on the list of Tasks given.
         */
        AddressBook generateAddressBook(List<Task> tasks) throws Exception{
            AddressBook addressBook = new AddressBook();
            addToAddressBook(addressBook, tasks);
            return addressBook;
        }

        /**
         * Adds auto-generated Task objects to the given AddressBook
         * @param addressBook The AddressBook to which the Tasks will be added
         */
        void addToAddressBook(AddressBook addressBook, int numGenerated) throws Exception{
            addToAddressBook(addressBook, generateTaskList(numGenerated));
        }

        /**
         * Adds the given list of Tasks to the given AddressBook
         */
        void addToAddressBook(AddressBook addressBook, List<Task> tasksToAdd) throws Exception{
            for(Task p: tasksToAdd){
                addressBook.addTask(p);
            }
        }

        /**
         * Adds auto-generated Task objects to the given model
         * @param model The model to which the Tasks will be added
         */
        void addToModel(Model model, int numGenerated) throws Exception{
            addToModel(model, generateTaskList(numGenerated));
        }

        /**
         * Adds the given list of Tasks to the given model
         */
        void addToModel(Model model, List<Task> tasksToAdd) throws Exception{
            for(Task p: tasksToAdd){
                model.addTask(p);
            }
        }

        /**
         * Generates a list of Tasks based on the flags.
         */
        List<Task> generateTaskList(int numGenerated) throws Exception{
            List<Task> tasks = new ArrayList<>();
            for(int i = 1; i <= numGenerated; i++){
                tasks.add(generateTask(i));
            }
            return tasks;
        }

        List<Task> generateTaskList(Task... tasks) {
            return Arrays.asList(tasks);
        }

        /**
         * Generates a Task object with given name. Other fields will have some dummy values.
         */
        Task generateTaskWithName(String name) throws Exception {
            return new Task(
                    new Name(name),
                    new DueDate("1"),
                    new Address("House of 1"),
                    new Priority("1"),
                    new UniqueTagList(new Tag("tag"))
            );
        }
    }
}
