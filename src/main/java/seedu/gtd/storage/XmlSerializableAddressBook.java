package seedu.gtd.storage;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import seedu.gtd.commons.exceptions.IllegalValueException;
import seedu.gtd.model.ReadOnlyAddressBook;
import seedu.gtd.model.task.ReadOnlyTask;
import seedu.gtd.model.task.UniqueTaskList;
import seedu.gtd.model.tag.Tag;
import seedu.gtd.model.tag.UniqueTagList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * An Immutable AddressBook that is serializable to XML format
 */
@XmlRootElement(name = "addressbook")
public class XmlSerializableAddressBook implements ReadOnlyAddressBook {

    @XmlElement
    private List<XmlAdaptedTask> tasks;
    @XmlElement
    private List<Tag> tags;

    {
        tasks = new ArrayList<>();
        tags = new ArrayList<>();
    }

    /**
     * Empty constructor required for marshalling
     */
    public XmlSerializableAddressBook() {}

    /**
     * Conversion
     */
    public XmlSerializableAddressBook(ReadOnlyAddressBook src) {
        tasks.addAll(src.getTaskList().stream().map(XmlAdaptedTask::new).collect(Collectors.toList()));
        tags = src.getTagList();
    }

    @Override
    public UniqueTagList getUniqueTagList() {
        try {
            return new UniqueTagList(tags);
        } catch (UniqueTagList.DuplicateTagException e) {
            //TODO: better error handling
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public UniqueTaskList getUniqueTaskList() {
        UniqueTaskList lists = new UniqueTaskList();
        for (XmlAdaptedTask p : tasks) {
            try {
                lists.add(p.toModelType());
            } catch (IllegalValueException e) {
                //TODO: better error handling
            }
        }
        return lists;
    }

    @Override
    public List<ReadOnlyTask> getTaskList() {
        return tasks.stream().map(p -> {
            try {
                return p.toModelType();
            } catch (IllegalValueException e) {
                e.printStackTrace();
                //TODO: better error handling
                return null;
            }
        }).collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public List<Tag> getTagList() {
        return Collections.unmodifiableList(tags);
    }

}
